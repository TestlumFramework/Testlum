package com.knubisoft.testlum.testing.framework.inbound;

import com.knubisoft.testlum.testing.framework.inbound.dto.TestSession;
import com.knubisoft.testlum.testing.framework.inbound.server.handler.impl.RegistryRequestHandler;
import com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer;
import com.knubisoft.testlum.testing.framework.inbound.server.server.impl.JdkInboundServer;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.Subscription;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.SubscriptionRegistry;
import com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Unit test for the subscription registry functionality.
 * This test validates the behavior of the subscription registry in routing and handling
 * inbound requests through registered subscriptions.
 * <p>
 * The test ensures the following:
 * - Subscriptions are correctly registered and matched based on a correlation ID.
 * - Requests are routed to the appropriate session handler based on the subscription match.
 * - Unmatched requests are handled with a 404 response.
 * - The session handlers properly receive and process requests.
 * - The number of active subscriptions is maintained correctly throughout the test.
 */
public class SubscriptionRegistryTest {

    private static final String TEST_ENDPOINT = "/webhooks/payment";
    private static final String TEST_ID_FOR_SESSION_A = "t-A";
    private static final String TEST_ID_FOR_SESSION_B = "t-B";

    public static void main(String[] args) throws Exception {
        SubscriptionRegistry registry = new SubscriptionRegistry(true);
        InboundServer server = new JdkInboundServer(
                "127.0.0.1", 0, 10, new RegistryRequestHandler(registry));
        server.start();
        try {
            testRegistry(server, registry);
        } finally {
            server.stop(Duration.ofSeconds(1));
        }
    }

    private static void testRegistry(InboundServer server, SubscriptionRegistry registry) throws Exception {
        String base = "http://127.0.0.1:" + server.getPort() + TEST_ENDPOINT;

        TestSession sessionA = new TestSession(TEST_ENDPOINT,"TEST-A");
        TestSession sessionB = new TestSession(TEST_ENDPOINT,"TEST-B");
        registry.register(new Subscription(TEST_ENDPOINT, TEST_ID_FOR_SESSION_A,
                CorrelationMatcher.header("X-Test-Id", TEST_ID_FOR_SESSION_A,false), sessionA));
        registry.register(new Subscription(TEST_ENDPOINT, TEST_ID_FOR_SESSION_B,
                CorrelationMatcher.header("X-Test-Id", TEST_ID_FOR_SESSION_B,false), sessionB));

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        HttpResponse<String> response1 = sendPost(client, base, TEST_ID_FOR_SESSION_B, "{\"evt\":\"b\"}");
        HttpResponse<String> response2 = sendPost(client, base, TEST_ID_FOR_SESSION_A, "{\"evt\":\"a\"}");
        HttpResponse<String> response3 = sendPost(client, base, null,  "{\"evt\":\"?\"}");

        if (response1.body().contains("TEST-B")) {
            System.out.println("[PASS] B routed to TEST-B");
        } else {
            throw new AssertionError("check failed. test case: B routed to TEST-B");
        }

        if (response2.body().contains("TEST-A")) {
            System.out.println("[PASS] A routed to TEST-A");
        } else {
            throw new AssertionError("checks failed. test case: A routed to TEST-A");
        }

        if (response3.statusCode() == 404) {
            System.out.println("[PASS] untagged -> 404");
        } else {
            throw new AssertionError("checks failed. test case: untagged -> 404");
        }

        if (sessionA.getRequest().get() != null
                && new String(sessionA.getRequest().get().getBody()).contains("\"a\"")) {
            System.out.println("[PASS] session A got its own body");
        } else {
            throw new AssertionError("checks failed. test case: session A got its own body");
        }

        if (sessionB.getRequest().get() != null
                && new String(sessionB.getRequest().get().getBody()).contains("\"b\"")) {
            System.out.println("[PASS] session B got its own body");
        } else {
            throw new AssertionError("checks failed. test case: session B got its own body");
        }

        if (registry.getActiveCount() == 2) {
            System.out.println("[PASS] active subscriptions still 2");
        } else {
            throw new AssertionError("checks failed. test case: active subscriptions still 2");
        }

        System.out.println( "\nALL CHECKS PASSED");
    }

    private static HttpResponse<String> sendPost(HttpClient c, String url, String testId, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (testId != null) {
            builder.header("X-Test-Id", testId);
        }
        return c.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

}
