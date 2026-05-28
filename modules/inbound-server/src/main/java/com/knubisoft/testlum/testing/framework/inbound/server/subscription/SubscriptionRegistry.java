package com.knubisoft.testlum.testing.framework.inbound.server.subscription;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Register of the active subscriptions. Solves parallel execution problem.
 * <p>
 * Base structure looks like {@code path -> list of subscriptions}. It represents
 * that multiple parallel test scenarios could listen on the same path. Incoming requests
 * will be routed to the appropriate subscription whose {@link com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher}
 * matches the request.
 * <p>
 * Concurrent map and copy-on-write list are used because multiple threads could access register
 * at the same time. Handler-threads of the server could read the register to find related subscriptions.
 * Test-threads could register/unregister their subscriptions.
 */
public class SubscriptionRegistry {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Subscription>> subscriptionsByPath
            = new ConcurrentHashMap<>();

    // Whether to allow parallel test executions on the same path. True by default
    private final boolean allowConcurrentOnSamePath;


    public SubscriptionRegistry(boolean allowConcurrentOnSamePath) {
        this.allowConcurrentOnSamePath = allowConcurrentOnSamePath;
    }

    public void register(Subscription subscription) {
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByPath
                .computeIfAbsent(subscription.getPath(), k -> new CopyOnWriteArrayList<>());
        if (!allowConcurrentOnSamePath && !subscriptions.isEmpty()) {
            // Log it: "Concurrent subscription on the same path is not allowed"
            return;
        }
        subscriptions.add(subscription);
        // Log that new subscription has been registered on the path
    }

    public void unregister(Subscription subscription) {
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByPath.get(subscription.getPath());
        if (subscriptions != null) {
            subscriptions.remove(subscription);
            if (subscriptions.isEmpty()) {
                subscriptionsByPath.remove(subscription.getPath(), subscriptions);
            }
        }
    }

    /**
     * To find a subscription for the incoming request.
     * <p>
     * Matching rules: if the matcher matches 2 subscriptions, it's a conflict on correlation ID.
     * In such case, we're just skipping the request so that tests will fail and none of the other
     * tests will get a wrong request.
     */
    public Optional<Subscription> route(InboundRequest request) {
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByPath.get(request.getPath());
        if (subscriptions == null) {
            return Optional.empty();
        }
        Subscription matchSubscription = null;
        int matchCount = 0;
        for (Subscription subscription : subscriptions) {
            if (subscription.matches(request)) {
                matchCount++;
                if (matchSubscription == null) {
                    matchSubscription = subscription;
                }
            }
        }

        if (matchCount == 0) {
            // Log that subscription has not been found
            return Optional.empty();
        }
        if (matchCount > 1) {
            // Log that there are multiple subscriptions that match the request
            return Optional.empty();
        }
        return Optional.of(matchSubscription);
    }

    /**
     * Unregister all subscriptions of the testing thread.
     */
    public void unregisterAllForTest(String testId) {
        subscriptionsByPath.values().forEach(subscriptions -> {
            subscriptions.removeIf(subscription -> subscription.getTestId().equals(testId));
        });
        subscriptionsByPath.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public int getActiveCount() {
        return subscriptionsByPath.values().stream().mapToInt(List::size).sum();
    }
}
