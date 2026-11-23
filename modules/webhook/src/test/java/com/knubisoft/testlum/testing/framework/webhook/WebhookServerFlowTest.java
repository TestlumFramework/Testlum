package com.knubisoft.testlum.testing.framework.webhook;

import com.knubisoft.testlum.testing.framework.inbound.server.server.impl.JdkInboundServer;
import com.knubisoft.testlum.testing.framework.webhook.matcher.WebhookMatchStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookServerFlowTest {

    private static final Duration HOLD = Duration.ofSeconds(2);

    private final HttpClient sender = HttpClient.newHttpClient();
    private final WebhookBuffer buffer = new WebhookBuffer(100, Duration.ofMinutes(5));
    private final WebhookEndpoint endpoint = new WebhookEndpoint(
            "https://my.ngrok.app/hooks/payment", 200, HOLD, WebhookMatcherType.BODY_KEY, "userId");
    private JdkInboundServer server;

    @BeforeEach
    void startServer() {
        WebhookIntegrationRegistry registry = new WebhookIntegrationRegistry();
        registry.register("dev", "PAYMENT", endpoint);
        server = new JdkInboundServer(true, "127.0.0.1", 0, 10, new WebhookRequestHandler(registry, buffer));
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(Duration.ZERO);
    }

    @Test
    void senderGetsTheStatusOfTheStepThatClaimsTheWebhook() throws Exception {
        CompletableFuture<HttpResponse<String>> delivery = send("/hooks/payment", "{\"userId\": \"42\"}");

        BufferedWebhook claimed = claim("42", Duration.ofSeconds(5));
        claimed.reply(503);

        assertEquals(503, delivery.get(5, TimeUnit.SECONDS).statusCode());
        assertEquals("{\"userId\": \"42\"}", new String(claimed.getRequest().getBody(), StandardCharsets.UTF_8));
    }

    @Test
    void retriesCanBeTestedWithConsecutiveSteps() throws Exception {
        CompletableFuture<HttpResponse<String>> first = send("/hooks/payment", "{\"userId\": \"42\"}");
        claim("42", Duration.ofSeconds(5)).reply(500);
        assertEquals(500, first.get(5, TimeUnit.SECONDS).statusCode());

        CompletableFuture<HttpResponse<String>> retry = send("/hooks/payment", "{\"userId\": \"42\"}");
        claim("42", Duration.ofSeconds(5)).reply(200);
        assertEquals(200, retry.get(5, TimeUnit.SECONDS).statusCode());
    }

    @Test
    void webhookArrivingBeforeTheStepIsHeldUntilClaimed() throws Exception {
        CompletableFuture<HttpResponse<String>> delivery = send("/hooks/payment", "{\"userId\": \"42\"}");
        TimeUnit.MILLISECONDS.sleep(500);
        assertTrue(!delivery.isDone(), "the sender is still waiting for the reply");

        claim("42", Duration.ZERO).reply(500);

        assertEquals(500, delivery.get(5, TimeUnit.SECONDS).statusCode());
    }

    @Test
    void unclaimedWebhookGetsDefaultStatusAfterHoldButCanStillBeValidated() throws Exception {
        CompletableFuture<HttpResponse<String>> delivery = send("/hooks/payment", "{\"userId\": \"42\"}");

        assertEquals(200, delivery.get(HOLD.toSeconds() + 5, TimeUnit.SECONDS).statusCode());
        BufferedWebhook late = claim("42", Duration.ZERO);
        assertEquals(200, late.reply(500), "too late: the sender already got the default status");
    }

    @Test
    void webhooksOfOtherScenariosAreNotClaimed() throws Exception {
        CompletableFuture<HttpResponse<String>> other = send("/hooks/payment", "{\"userId\": \"7\"}");
        CompletableFuture<HttpResponse<String>> mine = send("/hooks/payment", "{\"userId\": \"42\"}");

        claim("42", Duration.ofSeconds(5)).reply(201);
        claim("7", Duration.ofSeconds(5)).reply(202);

        assertEquals(201, mine.get(5, TimeUnit.SECONDS).statusCode());
        assertEquals(202, other.get(5, TimeUnit.SECONDS).statusCode());
    }

    @Test
    void unknownPathIsNotFound() throws Exception {
        assertEquals(404, send("/hooks/unknown", "{}").get(5, TimeUnit.SECONDS).statusCode());
    }

    private BufferedWebhook claim(final String userId, final Duration timeout) {
        WebhookMatchStrategy matcher = WebhookMatchers.forStep("PAYMENT", endpoint, userId);
        return buffer.claim(endpoint, matcher, timeout).orElseThrow();
    }

    private CompletableFuture<HttpResponse<String>> send(final String path, final String body) {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getPort() + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return sender.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
