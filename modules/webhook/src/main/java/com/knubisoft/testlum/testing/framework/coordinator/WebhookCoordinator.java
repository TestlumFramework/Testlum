package com.knubisoft.testlum.testing.framework.coordinator;

import com.knubisoft.testing.framework.coordinator.AbstractHttpInboundCoordinator;
import com.knubisoft.testing.framework.producer.ResponseProducer;
import com.knubisoft.testing.framework.session.SessionListener;
import com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.Subscription;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.SubscriptionRegistry;
import com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher;
import com.knubisoft.testlum.testing.framework.retry.RetryAttempt;
import com.knubisoft.testlum.testing.framework.retry.RetryPlan;
import com.knubisoft.testlum.testing.framework.session.WebhookCapturingSession;
import com.knubisoft.testlum.testing.framework.session.WebhookExchange;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class WebhookCoordinator extends AbstractHttpInboundCoordinator {

    private final ScheduledExecutorService scheduler;

    public WebhookCoordinator(InboundServer server, SubscriptionRegistry registry) {
        super(server, registry);
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

    @Override
    public WebhookExchange awaitOneRequest(String endpoint, String testId, CorrelationMatcher matcher,
                                           Duration timeout, ResponseProducer producer) {
        RetryAttempt initialRequest = new RetryAttempt(null, producer);
        return awaitOneRequest(endpoint, testId, matcher, RetryPlan.single(initialRequest, timeout), null);
    }

    public WebhookExchange awaitOneRequest(String endpoint, String testId, CorrelationMatcher matcher,
                                            RetryPlan retryPlan, SessionListener listener) {
        WebhookCapturingSession session = new WebhookCapturingSession(endpoint, retryPlan, scheduler);
        if (listener != null) {
            session.setListener(listener);
        }
        Subscription subscription = new Subscription(endpoint, testId, matcher, session);
        registry.register(subscription);
        try {
            return session.awaitExchange();
        } finally {
            registry.unregister(subscription);
        }
    }
}
