package com.knubisoft.testlum.testing.framework.webhook;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.webhook.matcher.WebhookMatchStrategy;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Received webhooks that no <webhook> step has claimed yet, kept per endpoint in arrival order.
 * <p>
 * Webhooks are buffered because they often arrive before the step that waits for them starts.
 * Claimed webhooks leave the buffer; unclaimed ones are dropped after the retention period
 * or when the endpoint's buffer is full.
 */
@Slf4j
public class WebhookBuffer {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int maxPerEndpoint;
    private final Duration retention;

    public WebhookBuffer(final int maxPerEndpoint, final Duration retention) {
        this.maxPerEndpoint = Math.max(1, maxPerEndpoint);
        this.retention = retention;
    }

    public BufferedWebhook add(final InboundRequest request, final WebhookEndpoint endpoint) {
        BufferedWebhook webhook = new BufferedWebhook(request, endpoint);
        Bucket bucket = bucket(endpoint);
        bucket.lock.lock();
        try {
            dropExpired(bucket);
            while (bucket.webhooks.size() >= maxPerEndpoint) {
                BufferedWebhook dropped = bucket.webhooks.removeFirst();
                dropped.reply(endpoint.getResponseStatusCode());
                log.warn("Webhook buffer for {} is full ({}), dropping the oldest unclaimed webhook: {}",
                        endpoint, maxPerEndpoint, dropped.getRequest());
            }
            bucket.webhooks.addLast(webhook);
            bucket.arrived.signalAll();
        } finally {
            bucket.lock.unlock();
        }
        return webhook;
    }

    /**
     * Takes the oldest buffered webhook of the endpoint that matches, waiting up to timeout
     */
    public Optional<BufferedWebhook> claim(final WebhookEndpoint endpoint,
                                          final WebhookMatchStrategy matcher,
                                          final Duration timeout) {
        Bucket bucket = bucket(endpoint);
        long deadline = System.nanoTime() + Math.max(0L, timeout.toNanos());
        bucket.lock.lock();
        try {
            while (true) {
                dropExpired(bucket);
                Optional<BufferedWebhook> claimed = takeFirstMatching(bucket, matcher);
                if (claimed.isPresent()) {
                    return claimed;
                }
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0L) {
                    return Optional.empty();
                }
                bucket.arrived.awaitNanos(remaining);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } finally {
            bucket.lock.unlock();
        }
    }

    /**
     * Number of unclaimed webhooks of the endpoint.
     */
    public int size(final WebhookEndpoint endpoint) {
        Bucket bucket = bucket(endpoint);
        bucket.lock.lock();
        try {
            dropExpired(bucket);
            return bucket.webhooks.size();
        } finally {
            bucket.lock.unlock();
        }
    }

    private Optional<BufferedWebhook> takeFirstMatching(final Bucket bucket, final WebhookMatchStrategy matcher) {
        Iterator<BufferedWebhook> iterator = bucket.webhooks.iterator();
        while (iterator.hasNext()) {
            BufferedWebhook webhook = iterator.next();
            if (matches(matcher, webhook)) {
                iterator.remove();
                return Optional.of(webhook);
            }
        }
        return Optional.empty();
    }

    private boolean matches(final WebhookMatchStrategy matcher, final BufferedWebhook webhook) {
        try {
            return matcher.matches(webhook.getRequest());
        } catch (RuntimeException e) {
            log.debug("Webhook matcher failed on {}: {}", webhook.getRequest(), e.getMessage());
            return false;
        }
    }

    private void dropExpired(final Bucket bucket) {
        Iterator<BufferedWebhook> iterator = bucket.webhooks.iterator();
        while (iterator.hasNext()) {
            BufferedWebhook webhook = iterator.next();
            if (webhook.getAge().compareTo(retention) <= 0) {
                return;
            }
            iterator.remove();
            webhook.reply(webhook.getEndpoint().getResponseStatusCode());
            log.info("Dropping webhook nobody claimed within {}s: {}", retention.toSeconds(), webhook.getRequest());
        }
    }

    private Bucket bucket(final WebhookEndpoint endpoint) {
        return buckets.computeIfAbsent(endpoint.getRoutingKey(), k -> new Bucket());
    }

    private static final class Bucket {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition arrived = lock.newCondition();
        private final Deque<BufferedWebhook> webhooks = new ArrayDeque<>();
    }
}
