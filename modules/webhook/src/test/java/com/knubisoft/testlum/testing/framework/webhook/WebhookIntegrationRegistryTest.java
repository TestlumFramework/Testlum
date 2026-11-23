package com.knubisoft.testlum.testing.framework.webhook;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.knubisoft.testlum.testing.framework.webhook.WebhookEndpointTest.endpoint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookIntegrationRegistryTest {

    @Test
    void samePathInSeveralEnvironmentsIsOneRoute() {
        WebhookIntegrationRegistry registry = new WebhookIntegrationRegistry();
        registry.register("dev", "PAYMENT", endpoint("https://dev.app/hooks/pay", WebhookMatcherType.NONE, null));
        registry.register("stage", "PAYMENT", endpoint("https://stage.app/hooks/pay", WebhookMatcherType.NONE, null));

        assertEquals(1, registry.getEndpoints().size());
        assertSame(registry.find("PAYMENT", "dev").orElseThrow(), registry.find("PAYMENT", "stage").orElseThrow());
        assertTrue(registry.find("PAYMENT", "prod").isEmpty());
    }

    @Test
    void samePathWithDifferentSettingsIsRejected() {
        WebhookIntegrationRegistry registry = new WebhookIntegrationRegistry();
        registry.register("dev", "A", endpoint("https://x.app/hooks/pay", WebhookMatcherType.NONE, null));
        WebhookEndpoint otherStatus = new WebhookEndpoint("https://x.app/hooks/pay", 202,
                Duration.ofSeconds(5), WebhookMatcherType.NONE, null);

        assertThrows(DefaultFrameworkException.class, () -> registry.register("dev", "B", otherStatus));
    }

    @Test
    void exactPathWinsOverPathWithVariable() {
        WebhookIntegrationRegistry registry = new WebhookIntegrationRegistry();
        WebhookEndpoint byId = endpoint("https://x.app/orders/{id}", WebhookMatcherType.PATH_VAR, "id");
        WebhookEndpoint latest = endpoint("https://x.app/orders/latest", WebhookMatcherType.NONE, null);
        registry.register("dev", "BY_ID", byId);
        registry.register("dev", "LATEST", latest);

        assertSame(latest, registry.findByRequestPath("/orders/latest").orElseThrow());
        assertSame(byId, registry.findByRequestPath("/orders/42").orElseThrow());
        assertTrue(registry.findByRequestPath("/payments/42").isEmpty());
    }
}
