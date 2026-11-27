package com.testlum.testing.framework.webhook;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.testlum.testing.framework.webhook.matcher.WebhookMatchStrategy;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.testlum.testing.framework.webhook.WebhookEndpointTest.endpoint;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookMatchersTest {

    @Test
    void headerNameIsCaseInsensitive() {
        WebhookMatchStrategy matcher = WebhookMatchers.forStep("A",
                endpoint("https://x.app/hook", WebhookMatcherType.HEADER, "X-User-Id"), "42");
        assertTrue(matcher.matches(request("/hook", Map.of("x-user-id", List.of("42")), Map.of(), "")));
        assertFalse(matcher.matches(request("/hook", Map.of("X-User-Id", List.of("43")), Map.of(), "")));
        assertFalse(matcher.matches(request("/hook", Map.of(), Map.of(), "")));
    }

    @Test
    void queryParam() {
        WebhookMatchStrategy matcher = WebhookMatchers.forStep("A",
                endpoint("https://x.app/hook", WebhookMatcherType.QUERY_PARAM, "userId"), "42");
        assertTrue(matcher.matches(request("/hook", Map.of(), Map.of("userId", List.of("42")), "")));
        assertFalse(matcher.matches(request("/hook", Map.of(), Map.of("userId", List.of("7")), "")));
    }

    @Test
    void bodyKeyAcceptsPlainKeysAndJsonPaths() {
        String body = "{\"user\": {\"id\": 42}, \"status\": \"paid\"}";
        WebhookMatchStrategy nested = WebhookMatchers.forStep("A",
                endpoint("https://x.app/hook", WebhookMatcherType.BODY_KEY, "user.id"), "42");
        WebhookMatchStrategy jsonPath = WebhookMatchers.forStep("A",
                endpoint("https://x.app/hook", WebhookMatcherType.BODY_KEY, "$.status"), "paid");
        assertTrue(nested.matches(request("/hook", Map.of(), Map.of(), body)));
        assertTrue(jsonPath.matches(request("/hook", Map.of(), Map.of(), body)));
    }

    @Test
    void bodyKeyDoesNotThrowOnMissingKeyOrNonJsonBody() {
        WebhookMatchStrategy matcher = WebhookMatchers.forStep("A",
                endpoint("https://x.app/hook", WebhookMatcherType.BODY_KEY, "user.id"), "42");
        assertFalse(matcher.matches(request("/hook", Map.of(), Map.of(), "{\"other\": 1}")));
        assertFalse(matcher.matches(request("/hook", Map.of(), Map.of(), "user_id=42")));
        assertFalse(matcher.matches(request("/hook", Map.of(), Map.of(), "")));
    }

    @Test
    void pathVariable() {
        WebhookMatchStrategy matcher = WebhookMatchers.forStep("A",
                endpoint("https://x.app/orders/{orderId}", WebhookMatcherType.PATH_VAR, "orderId"), "42");
        assertTrue(matcher.matches(request("/orders/42", Map.of(), Map.of(), "")));
        assertFalse(matcher.matches(request("/orders/420", Map.of(), Map.of(), "")));
    }

    @Test
    void matcherWithoutMatchValueIsAConfigurationError() {
        WebhookEndpoint endpoint = endpoint("https://x.app/hook", WebhookMatcherType.HEADER, "X-User-Id");
        assertThrows(DefaultFrameworkException.class, () -> WebhookMatchers.forStep("A", endpoint, null));
        assertThrows(DefaultFrameworkException.class, () -> WebhookMatchers.forStep("A", endpoint, " "));
    }

    @Test
    void noMatcherMatchesEverything() {
        WebhookMatchStrategy matcher = WebhookMatchers.forStep("A",
                endpoint("https://x.app/hook", WebhookMatcherType.NONE, null), null);
        assertTrue(matcher.matches(request("/hook", Map.of(), Map.of(), "anything")));
    }

    static InboundRequest request(final String path,
                                  final Map<String, List<String>> headers,
                                  final Map<String, List<String>> query,
                                  final String body) {
        return new InboundRequest("POST", path, null, headers, query, body.getBytes(StandardCharsets.UTF_8));
    }
}
