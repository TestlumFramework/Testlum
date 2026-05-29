package com.knubisoft.testlum.testing.framework.coordinator;

import com.knubisoft.testing.framework.coordinator.AbstractHttpInboundCoordinator;
import com.knubisoft.testing.framework.coordinator.InboundDestination;
import com.knubisoft.testing.framework.producer.ResponseProducer;
import com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.Subscription;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.SubscriptionRegistry;
import com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher;
import com.knubisoft.testlum.testing.framework.retry.RetryAttempt;
import com.knubisoft.testlum.testing.framework.retry.RetryPlan;
import com.knubisoft.testlum.testing.framework.session.WebhookCapturingSession;
import com.knubisoft.testlum.testing.framework.session.WebhookExchange;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class WebhookCoordinator extends AbstractHttpInboundCoordinator {

    private final ScheduledExecutorService scheduler;

    public WebhookCoordinator(InboundServer server, SubscriptionRegistry registry, Map<String, InboundDestination> destinationsByAliasEnv) {
        super(server, registry, destinationsByAliasEnv);
        AtomicLong n = new AtomicLong();
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "testlum-inbound-timer-" + n.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void stop() {
        super.stop();
        scheduler.shutdownNow();
        try {
            scheduler.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public WebhookExchange awaitOneRequest(String endpoint, String testId, CorrelationMatcher matcher,
                                           RetryPlan retryPlan) {
        WebhookCapturingSession session = new WebhookCapturingSession(endpoint, retryPlan, scheduler);
        Subscription subscription = new Subscription(endpoint, testId, matcher, session);
        registry.register(subscription);
        try {
            return session.awaitExchange();
        } finally {
            registry.unregister(subscription);
        }
    }

    @Override
    public WebhookExchange awaitOneRequest(String endpoint, String testId, CorrelationMatcher matcher,
                                           Duration timeout, ResponseProducer producer) {
        RetryAttempt initialRequest = new RetryAttempt(null, producer);
        return awaitOneRequest(endpoint, testId, matcher, RetryPlan.single(initialRequest, timeout));
    }
}
