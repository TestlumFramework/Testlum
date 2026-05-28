package com.knubisoft.testlum.testing.framework.inbound.server.handler.impl;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.handler.InboundRequestHandler;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.Subscription;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.SubscriptionRegistry;
import com.knubisoft.testlum.testing.framework.inbound.server.util.InboundResponseFactory;

/**
 * Implementation of {@link InboundRequestHandler} that routes requests to
 * the correct {@link Subscription} with the help of {@link SubscriptionRegistry}.
 * <p>
 * It links our {@link com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer}
 * with the scenario engine. The server only knows about {@link RegistryRequestHandler}.
 * Correlation, matcher, subscription, scenarios are located on the other side of the {@link SubscriptionRegistry}.
 */
public class RegistryRequestHandler implements InboundRequestHandler {

    private final SubscriptionRegistry registry;

    public RegistryRequestHandler(SubscriptionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public InboundResponse handle(InboundRequest request) {
        return registry.route(request)
                .map(subscription -> deliver(subscription, request))
                .orElseGet(() -> InboundResponseFactory.notFound(
                        "No active inbound session for " + request.getMethod() + " " + request.getPath()));
    }

    private InboundResponse deliver(Subscription subscription, InboundRequest request) {
        try {
            return subscription.deliver(request);
        } catch (Exception e) {
            return InboundResponseFactory.internalError("session failed handling request: " + e.getMessage());
        }
    }

}
