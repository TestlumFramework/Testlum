package com.knubisoft.testing.framework.coordinator;

import com.knubisoft.testing.framework.producer.ResponseProducer;
import com.knubisoft.testing.framework.session.CapturingSession;
import com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer;
import com.knubisoft.testing.framework.session.SingleInboundExchange;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.Subscription;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.SubscriptionRegistry;
import com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;


@Component
public class HttpInboundCoordinator extends AbstractHttpInboundCoordinator {


    public HttpInboundCoordinator(InboundServer server,
                                  SubscriptionRegistry registry,
                                  Map<String, InboundDestination> destinationsByAliasEnv) {
        super(server, registry, destinationsByAliasEnv);
    }

    @Override
    public SingleInboundExchange awaitOneRequest(String endpoint, String testId, CorrelationMatcher matcher,
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
