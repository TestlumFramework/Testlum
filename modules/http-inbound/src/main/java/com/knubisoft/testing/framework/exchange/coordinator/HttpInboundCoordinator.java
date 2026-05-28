package com.knubisoft.testing.framework.exchange.coordinator;

import com.knubisoft.testing.framework.exchange.producer.ResponseProducer;
import com.knubisoft.testing.framework.exchange.session.CapturingSession;
import com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchange;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.Subscription;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.SubscriptionRegistry;
import com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher;

import java.time.Duration;
import java.util.Map;

/**
 * Coordinator of the whole inbound request flow. It creates a server and a registry.
 * Interpretator uses this bean and register/wait for the incoming requests.
 */
public class HttpInboundCoordinator {

    private final InboundServer server;
    private final SubscriptionRegistry registry;
    // alias+env -> destination
    private final Map<String, InboundDestination> destinationsByAliasEnv;


    public HttpInboundCoordinator(InboundServer server,
                                  SubscriptionRegistry registry,
                                  Map<String, InboundDestination> destinationsByAliasEnv) {
        this.server = server;
        this.registry = registry;
        this.destinationsByAliasEnv = destinationsByAliasEnv;
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(Duration.ofSeconds(5));
    }

    /**
     * Resolved webhook destination by alias and environment.
     */
    public InboundDestination resolve(String alias, String environment) {
        InboundDestination destination = destinationsByAliasEnv.get(getKey(alias, environment));
        if (destination == null) {
            // Log that alias has not been found
            return null;
        }
        return destination;
    }

    /**
     * Waits for a single inbound request (by registered session) and blocks scenario's thread until
     * the incoming request or timeout.
     *
     * @param endpoint the endpoint to listen to incoming requests.
     * @param testId the unique identifier (correlation ID) for the test scenario associated with the request.
     * @param matcher the correlation matcher used to find the request based on specific criteria.
     * @param timeout the duration to wait for the request before timing out.
     * @param producer the response producer strategy
     */
    public InboundExchange awaitOneRequest(String endpoint, String testId, CorrelationMatcher matcher,
                                           Duration timeout, ResponseProducer producer) {
        CapturingSession session = new CapturingSession(endpoint, timeout, producer);
        Subscription subscription = new Subscription(endpoint, testId, matcher, session);
        registry.register(subscription);
        try {
            return session.awaitExchange();
        } finally {
            registry.unregister(subscription);
        }
    }

    private String getKey(String alias, String environment) {
        return alias + "@" + environment;
    }
}
