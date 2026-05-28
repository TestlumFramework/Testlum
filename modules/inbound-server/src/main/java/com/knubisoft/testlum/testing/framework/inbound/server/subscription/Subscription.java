package com.knubisoft.testlum.testing.framework.inbound.server.subscription;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher;

/**
 * Subscription to incoming requests.
 * <p>
 * Thread related to {@code testId} is waiting for incoming requests
 * on {@code path} that satisfy {@code matcher}, and returning its {@code session}.
 * <p>
 * Multiple subscriptions can be related to the same {@code path}, and only {@code matcher}
 * correlates them.
 */
public class Subscription {

    private final String path;
    private final String testId;
    private final CorrelationMatcher matcher;
    private final InboundSession session;

    public Subscription(String path, String testId, CorrelationMatcher matcher, InboundSession session) {
        this.path = path;
        this.testId = testId;
        this.matcher = matcher;
        this.session = session;
    }

    public String getPath() {
        return path;
    }

    public String getTestId() {
        return testId;
    }

    public boolean matches(InboundRequest request) {
        return matcher.matches(request);
    }

    public InboundResponse deliver(InboundRequest request) {
        return session.onRequest(request);
    }

    public boolean isCompleted() {
        return session.isCompleted();
    }

    @Override
    public String toString() {
        return "Subscription{ path='" + path + ", testId='" + testId + '}';
    }
}
