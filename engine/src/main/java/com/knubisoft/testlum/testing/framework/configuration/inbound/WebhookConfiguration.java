package com.knubisoft.testlum.testing.framework.configuration.inbound;

import com.knubisoft.testlum.testing.framework.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.webhook.WebhookBuffer;
import com.knubisoft.testlum.testing.framework.webhook.WebhookEndpoint;
import com.knubisoft.testlum.testing.framework.webhook.WebhookIntegrationRegistry;
import com.knubisoft.testlum.testing.framework.webhook.WebhookMatcherType;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Matcher;
import com.knubisoft.testlum.testing.model.global_config.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Configuration
public class WebhookConfiguration {

    private static final int MAX_BUFFERED_PER_ENDPOINT = 100;
    private static final Duration UNCLAIMED_RETENTION = Duration.ofMinutes(5);

    @Bean
    public WebhookBuffer webhookBuffer() {
        return new WebhookBuffer(MAX_BUFFERED_PER_ENDPOINT, UNCLAIMED_RETENTION);
    }

    @Bean
    public WebhookIntegrationRegistry webhookIntegrationRegistry(final EnvToIntegrationMap envToIntegrations) {
        WebhookIntegrationRegistry registry = new WebhookIntegrationRegistry();
        envToIntegrations.forEach((env, integrations) -> registerWebhooks(registry, env, integrations));
        return registry;
    }

    private void registerWebhooks(final WebhookIntegrationRegistry registry,
                                  final String env,
                                  final Integrations integrations) {
        if (Objects.isNull(integrations) || Objects.isNull(integrations.getWebhooks())) {
            return;
        }
        integrations.getWebhooks().getWebhook().stream()
                .filter(Integration::isEnabled)
                .forEach(webhook -> {
                    WebhookEndpoint endpoint = toEndpoint(webhook);
                    registry.register(env, webhook.getAlias(), endpoint);
                    log.info("Webhook integration '{}' (env '{}') listens on {}", webhook.getAlias(), env, endpoint);
                });
    }

    private WebhookEndpoint toEndpoint(final Webhook webhook) {
        MatcherRule rule = resolveRule(webhook.getMatcher());
        return new WebhookEndpoint(webhook.getUrl(), webhook.getResponseStatusCode(),
                Duration.ofSeconds(webhook.getHoldTimeout()), rule.type(), rule.key());
    }

    private static MatcherRule resolveRule(final Matcher matcher) {
        if (matcher == null) {
            return MatcherRule.NONE;
        }
        return Stream.of(
                new MatcherRule(WebhookMatcherType.HEADER, matcher.getByHeader()),
                new MatcherRule(WebhookMatcherType.QUERY_PARAM, matcher.getByQueryParam()),
                new MatcherRule(WebhookMatcherType.BODY_KEY, matcher.getByBodyKey()),
                new MatcherRule(WebhookMatcherType.PATH_VAR, matcher.getByPathVar()))
           .filter(rule -> rule.key != null)
           .findFirst()
           .orElse(MatcherRule.NONE);
    }

    private record MatcherRule(WebhookMatcherType type, String key) {
        static final MatcherRule NONE = new MatcherRule(WebhookMatcherType.NONE, null);
    }
}
