package com.knubisoft.testlum.testing.framework.retry;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Represents a strategy of the Webhook command processing. It may or may not include
 * retry attempts.
 * <p>
 * If retries are not set, only {@link #initialAttempt} is used and represents the single
 * Webhook command. Otherwise, the {@link #retryAttempts} are used to retry the command
 * after main {@link #initialAttempt}.
 * <p>
 * {@link #initialAttempt} is required.
 * {@link #retryAttempts} is the list of retry attempts.
 * {@link #minRetries} is the minimum number of retries that sender has to execute.
 *  If the total number of retries is less than this, there will be the failure of the step.
 * {@link #idleTimeout} represents the time during which the sender should send new retry.
 *  It gets reset after each successful retry. If the retry is not sent, we're checking
 *  {@link #minRetries}. If the total number of retries is less than {@link #minRetries},
 *  there will be the failure of the step.
 * {@link #noRetryTimeout} represents the time during which the sender should not retry.
 *  If some retry is caught, the scenario step will fail.
 * {@link #overallTimeout} represents the top limit of the session duration.
 */
public class RetryPlan {

    private final RetryAttempt initialAttempt;
    private final List<RetryAttempt> retryAttempts;
    private final int minRetries;
    private final Duration idleTimeout;
    private final Duration noRetryTimeout;
    private final Duration overallTimeout;

    public RetryPlan(RetryAttempt initialAttempt, List<RetryAttempt> retryAttempts, int minRetries, Duration idleTimeout, Duration noRetryTimeout, Duration overallTimeout) {
        if (initialAttempt == null) {
            throw new DefaultFrameworkException("Initial attempt is required");
        }
        if (retryAttempts == null || retryAttempts.isEmpty()) {
            retryAttempts = List.of();
        }
        if (minRetries < 0 || minRetries > retryAttempts.size()) {
            throw new DefaultFrameworkException("minRetries must be between 0 and " + retryAttempts.size());
        }
        if (idleTimeout == null) {
            idleTimeout = Duration.ZERO;
        }
        if (noRetryTimeout == null) {
            noRetryTimeout = Duration.ZERO;
        }
        if (overallTimeout == null) {
            overallTimeout = Duration.ofSeconds(60);
        }

        this.initialAttempt = initialAttempt;
        this.retryAttempts = Collections.unmodifiableList(retryAttempts);
        this.minRetries = minRetries;
        this.idleTimeout = idleTimeout;
        this.noRetryTimeout = noRetryTimeout;
        this.overallTimeout = overallTimeout;
    }

    public RetryAttempt getInitialAttempt() {
        return initialAttempt;
    }

    public List<RetryAttempt> getRetryAttempts() {
        return retryAttempts;
    }

    public int getMinRetries() {
        return minRetries;
    }

    public int getMaxRetries() {
        return retryAttempts.size();
    }

    public Duration getIdleTimeout() {
        return idleTimeout;
    }

    public Duration getNoRetryTimeout() {
        return noRetryTimeout;
    }

    public Duration getOverallTimeout() {
        return overallTimeout;
    }

    public static RetryPlan single(RetryAttempt initialAttempt, Duration overallTimeout) {
        return new RetryPlan(initialAttempt, List.of(), 0, Duration.ZERO, Duration.ZERO, overallTimeout);
    }
}
