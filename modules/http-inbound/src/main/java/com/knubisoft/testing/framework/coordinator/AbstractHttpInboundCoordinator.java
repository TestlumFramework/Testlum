package com.knubisoft.testing.framework.coordinator;

import com.knubisoft.testing.framework.producer.ResponseProducer;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.AbstractInboundExchange;
import com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.SubscriptionRegistry;
import com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher;

import java.time.Duration;

/**
 * Coordinator of the whole inbound request flow. It creates a server and a registry.
 * Interpretator uses this bean and register/wait for the incoming requests.
 */
public abstract class AbstractHttpInboundCoordinator {

    private final InboundServer server;
    protected final SubscriptionRegistry registry;

    protected AbstractHttpInboundCoordinator(InboundServer server, SubscriptionRegistry registry) {
        this.server = server;
        this.registry = registry;
    }

    public void start() {
        if (server.isRunning()) {
            return;
        }
        server.start();
    }

    public void stop() {
        server.stop(Duration.ofSeconds(5));
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
    public abstract AbstractInboundExchange awaitOneRequest(String endpoint, String testId,
                                                            CorrelationMatcher matcher, Duration timeout,
                                                            ResponseProducer producer);

    private String getKey(String alias, String environment) {
        return alias + "@" + environment;
    }

}
