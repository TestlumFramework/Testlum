package com.testlum.testing.framework.webhook;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.util.WebhookUrlUtil;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookEndpointTest {

    @Test
    void usesOnlyThePathOfTheIntegrationUrl() {
        assertEquals("/hooks/payment", WebhookUrlUtil.pathOf("https://my.ngrok.app/hooks/payment"));
        assertEquals("/hooks/payment", WebhookUrlUtil.pathOf("http://localhost:8080/hooks/payment/?a=1#x"));
        assertEquals("/", WebhookUrlUtil.pathOf("https://my.ngrok.app"));
        assertEquals("/orders/{orderId}", WebhookUrlUtil.pathOf("https://my.ngrok.app/orders/{orderId}"));
    }

    @Test
    void exactPathMatchesWithOrWithoutTrailingSlash() {
        WebhookEndpoint endpoint = endpoint("https://x.app/hooks/payment", WebhookMatcherType.NONE, null);
        assertTrue(endpoint.matchesPath("/hooks/payment"));
        assertTrue(endpoint.matchesPath("/hooks/payment/"));
        assertFalse(endpoint.matchesPath("/hooks/payment/extra"));
        assertFalse(endpoint.matchesPath("/hooks"));
    }

    @Test
    void pathVariableMatchesExactlyOneSegment() {
        WebhookEndpoint endpoint = endpoint("https://x.app/orders/{orderId}/paid", WebhookMatcherType.NONE, null);
        assertTrue(endpoint.hasPathVariables());
        assertEquals("/orders/{}/paid", endpoint.getRoutingKey());
        assertTrue(endpoint.matchesPath("/orders/123/paid"));
        assertFalse(endpoint.matchesPath("/orders/1/2/paid"));
        assertFalse(endpoint.matchesPath("/orders//paid"));
        assertTrue(endpoint.pathPatternWith("orderId", "123").matcher("/orders/123/paid").matches());
        assertFalse(endpoint.pathPatternWith("orderId", "123").matcher("/orders/124/paid").matches());
    }

    @Test
    void pathVarMatcherMustNameAVariableOfTheUrl() {
        assertThrows(DefaultFrameworkException.class,
                () -> endpoint("https://x.app/orders/{orderId}", WebhookMatcherType.PATH_VAR, "id"));
    }

    static WebhookEndpoint endpoint(final String url, final WebhookMatcherType type, final String key) {
        return new WebhookEndpoint(url, 200, Duration.ofSeconds(5), type, key);
    }
}
