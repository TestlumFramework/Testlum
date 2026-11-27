package com.testlum.testing.framework.webhook;

import com.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.testlum.testing.framework.webhook.matcher.WebhookMatchStrategy;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.testlum.testing.framework.webhook.WebhookEndpointTest.endpoint;
import static com.testlum.testing.framework.webhook.WebhookMatchersTest.request;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookBufferTest {

    private final WebhookEndpoint endpoint =
            endpoint("https://x.app/hook", WebhookMatcherType.HEADER, "X-User-Id");
    private final WebhookBuffer buffer = new WebhookBuffer(100, Duration.ofMinutes(5));

    @Test
    void claimsWebhookThatArrivedBeforeTheStep() {
        BufferedWebhook arrived = buffer.add(webhookFor("42"), endpoint);

        Optional<BufferedWebhook> claimed = buffer.claim(endpoint, matcherFor("42"), Duration.ZERO);

        assertSame(arrived, claimed.orElseThrow());
        assertEquals(0, buffer.size(endpoint));
    }

    @Test
    void waitsForWebhookThatArrivesLater() throws Exception {
        CompletableFuture<Optional<BufferedWebhook>> step = CompletableFuture.supplyAsync(
                () -> buffer.claim(endpoint, matcherFor("42"), Duration.ofSeconds(5)));
        TimeUnit.MILLISECONDS.sleep(200);
        BufferedWebhook arrived = buffer.add(webhookFor("42"), endpoint);

        assertSame(arrived, step.get(5, TimeUnit.SECONDS).orElseThrow());
    }

    @Test
    void claimsOnlyMatchingWebhooksOldestFirst() {
        buffer.add(webhookFor("7"), endpoint);
        BufferedWebhook first = buffer.add(webhookFor("42"), endpoint);
        BufferedWebhook second = buffer.add(webhookFor("42"), endpoint);

        assertSame(first, buffer.claim(endpoint, matcherFor("42"), Duration.ZERO).orElseThrow());
        assertSame(second, buffer.claim(endpoint, matcherFor("42"), Duration.ZERO).orElseThrow());
        assertTrue(buffer.claim(endpoint, matcherFor("42"), Duration.ZERO).isEmpty());
        assertEquals(1, buffer.size(endpoint), "the non-matching webhook stays for its own step");
    }

    @Test
    void timesOutWhenNothingMatches() {
        buffer.add(webhookFor("7"), endpoint);
        long start = System.nanoTime();

        assertTrue(buffer.claim(endpoint, matcherFor("42"), Duration.ofMillis(300)).isEmpty());
        assertTrue(Duration.ofNanos(System.nanoTime() - start).toMillis() >= 250);
    }

    @Test
    void throwingMatcherDoesNotBreakTheStep() {
        buffer.add(webhookFor("42"), endpoint);
        WebhookMatchStrategy broken = request -> {
            throw new IllegalStateException("boom");
        };

        assertTrue(buffer.claim(endpoint, broken, Duration.ZERO).isEmpty());
    }

    @Test
    void dropsOldestWhenFullAndRepliesToItsSender() {
        WebhookBuffer small = new WebhookBuffer(2, Duration.ofMinutes(5));
        BufferedWebhook oldest = small.add(webhookFor("1"), endpoint);
        small.add(webhookFor("2"), endpoint);
        small.add(webhookFor("3"), endpoint);

        assertEquals(2, small.size(endpoint));
        assertTrue(oldest.isReplied());
        assertEquals(200, oldest.reply(500), "the sender already got the default status");
    }

    @Test
    void dropsWebhooksNobodyClaimedWithinRetention() throws Exception {
        WebhookBuffer shortRetention = new WebhookBuffer(100, Duration.ofMillis(100));
        shortRetention.add(webhookFor("42"), endpoint);
        TimeUnit.MILLISECONDS.sleep(200);

        assertTrue(shortRetention.claim(endpoint, matcherFor("42"), Duration.ZERO).isEmpty());
    }

    private InboundRequest webhookFor(final String userId) {
        return request("/hook", Map.of("X-User-Id", List.of(userId)), Map.of(), "{}");
    }

    private WebhookMatchStrategy matcherFor(final String userId) {
        return WebhookMatchers.forStep("A", endpoint, userId);
    }
}
