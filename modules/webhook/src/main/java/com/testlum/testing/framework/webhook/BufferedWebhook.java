package com.testlum.testing.framework.webhook;

import com.testlum.testing.framework.inbound.server.dto.InboundRequest;
import lombok.Getter;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A received webhook waiting in {@link WebhookBuffer} for a <webhook> step.
 * <p>
 * The sender's connection stays open while the webhook waits
 */
@Getter
public final class BufferedWebhook {

    private final InboundRequest request;
    private final WebhookEndpoint endpoint;
    private final long receivedAtNanos;
    private final CompletableFuture<Integer> replyStatus = new CompletableFuture<>();

    public BufferedWebhook(final InboundRequest request, final WebhookEndpoint endpoint) {
        this.request = request;
        this.endpoint = endpoint;
        this.receivedAtNanos = System.nanoTime();
    }

    /**
     * Server side: waits up to the hold timeout for a step to choose the status.
     * <p>
     * @return the status to send to the webhook sender
     */
    public int awaitReplyStatus() {
        Duration hold = endpoint.getHoldTimeout();
        if (!hold.isZero()) {
            try {
                return replyStatus.get(hold.toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException | ExecutionException e) {
                // reply with the default status
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return reply(endpoint.getResponseStatusCode());
    }

    /**
     * Sets the reply status unless it is already decided.
     * <p>
     * @return the status that the sender gets
     */
    public int reply(final int status) {
        replyStatus.complete(status);
        return replyStatus.join();
    }

    public boolean isReplied() {
        return replyStatus.isDone();
    }

    public Duration getAge() {
        return Duration.ofNanos(System.nanoTime() - receivedAtNanos);
    }
}
