package com.knubisoft.testlum.testing.framework.webhook;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Webhook integrations of all enabled environments.
 * <p>
 * All environments share one inbound server, so the same url path in several environments
 * is one route.
 */
public class WebhookIntegrationRegistry {

    private final Map<String, WebhookEndpoint> byAliasAndEnv = new ConcurrentHashMap<>();
    private final List<WebhookEndpoint> endpoints = new CopyOnWriteArrayList<>();

    public synchronized void register(final String env, final String alias, final WebhookEndpoint endpoint) {
        WebhookEndpoint route = findRoute(endpoint.getRoutingKey()).orElse(null);
        if (route == null) {
            endpoints.add(endpoint);
            route = endpoint;
        } else if (!route.sameSettingsAs(endpoint)) {
            throw duplicateIntegrationException(env, alias, endpoint);
        }
        byAliasAndEnv.put(key(alias, env), route);
    }

    private DefaultFrameworkException duplicateIntegrationException(final String env, final String alias,
                                                                    final WebhookEndpoint endpoint) {
        return new DefaultFrameworkException(
                String.format("Webhook integration '%s' (env '%s') uses path %s, which is already configured " +
                        "differently by another webhook integration. Webhook integrations sharing a path must " +
                        "have the same path, responseStatusCode, holdTimeout and matcher", alias, env, endpoint.getPath()));
    }

    public Optional<WebhookEndpoint> find(final String alias, final String env) {
        return Optional.ofNullable(byAliasAndEnv.get(key(alias, env)));
    }

    public Optional<WebhookEndpoint> findByRequestPath(final String requestPath) {
        Optional<WebhookEndpoint> exact = endpoints.stream()
                .filter(e -> !e.hasPathVariables() && e.matchesPath(requestPath))
                .findFirst();
        return exact.isPresent() ? exact : endpoints.stream()
                .filter(e -> e.hasPathVariables() && e.matchesPath(requestPath))
                .findFirst();
    }

    public List<WebhookEndpoint> getEndpoints() {
        return new ArrayList<>(endpoints);
    }

    private Optional<WebhookEndpoint> findRoute(final String routingKey) {
        return endpoints.stream().filter(e -> e.getRoutingKey().equals(routingKey)).findFirst();
    }

    private static String key(final String alias, final String env) {
        return alias + "@" + env;
    }
}
