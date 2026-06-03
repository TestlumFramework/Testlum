package com.knubisoft.testlum.testing.framework.retry;

import com.knubisoft.testing.framework.producer.ResponseProducer;
import com.knubisoft.testing.framework.producer.ResponseProducerFactory;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.ResponseProducerBuilder;
import com.knubisoft.testlum.testing.framework.util.ContentResolver;
import com.knubisoft.testlum.testing.model.scenario.Webhook;
import com.knubisoft.testlum.testing.model.scenario.WebhookExpected;
import com.knubisoft.testlum.testing.model.scenario.WebhookResponse;
import com.knubisoft.testlum.testing.model.scenario.WebhookRetry;
import com.knubisoft.testlum.testing.model.scenario.WebhookRetryAttempt;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RetryPlanFactory {

    private final ResponseProducerBuilder producerBuilder;

    public RetryPlanFactory(final ContentResolver contentResolver,
                            final ResponseProducerFactory producerFactory) {
        this.producerBuilder = new ResponseProducerBuilder(contentResolver, producerFactory);
    }

    public RetryPlan buildRetryPlan(final Webhook webhook, final String destinationUrl) {
        RetryAttempt initialRequest = buildAttempt(
                webhook.getReceive().getExpected(),
                webhook.getResponse(),
                webhook.getReceive().isForward(),
                destinationUrl);
        List<RetryAttempt> retryAttempts = buildRetries(webhook, destinationUrl);
        int minRetries = 0;
        if (webhook.getRetry() != null && webhook.getRetry().getMin() != null) {
            minRetries = webhook.getRetry().getMin();
        }
        if (minRetries > retryAttempts.size()) {
            throw new DefaultFrameworkException("<retry min=\"" + minRetries + "\"> " +
                    "exceeds available retries (" + retryAttempts.size() + ")");
        }
        Duration idleTimeout = Duration.ZERO;
        if (webhook.getRetry() != null) {
            idleTimeout = parseTimeout(webhook.getRetry().getIdleTimeout());
        }
        Duration noRetryTimeout = Duration.ZERO;
        if (webhook.getNoRetry() != null) {
            noRetryTimeout = parseTimeout(webhook.getNoRetry().getTimeout());
        }
        Duration overallTimeout = parseTimeout(webhook.getTimeout());
        return new RetryPlan(initialRequest, retryAttempts, minRetries,
                idleTimeout, noRetryTimeout, overallTimeout);
    }

    private RetryAttempt buildAttempt(final String expectedFile,
                                      final WebhookResponse response,
                                      final Boolean forward,
                                      final String destinationUrl) {
        ResponseProducer producer = producerBuilder.buildResponseProducer(forward, response, destinationUrl);
        WebhookExpected expectedDestination = (response != null) ? response.getExpected() : null;
        return new RetryAttempt(expectedFile, expectedDestination, producer);
    }

    private List<RetryAttempt> buildRetries(final Webhook webhook, final String destinationUrl) {
        WebhookRetry retry = webhook.getRetry();
        if (retry == null) {
            return List.of();
        }
        List<WebhookRetryAttempt> retryAttemptsDto = retry.getAttempt();
        boolean perAttemptRetry = retryAttemptsDto != null && !retryAttemptsDto.isEmpty();
        boolean commonRetryTemplate = retry.getReceive() != null;
        if (perAttemptRetry && commonRetryTemplate) {
            throw new DefaultFrameworkException("<retry> must contain either a template (<receive>+<response>) "
                    + "or <attempt> list, not both");
        }
        if (perAttemptRetry) {
            return getRetryPerAttemptRetries(destinationUrl, retryAttemptsDto);
        }
        if (commonRetryTemplate) {
            return getTemplateRetries(destinationUrl, retry, retryAttemptsDto);
        }
        return List.of();
    }

    /**
     * The method is responsible for building {@link RetryAttempt}s based on <attempt> tags:
     * <retry>
     *     <attempt>
     *         <receive ... />
     *         <response ... />
     *     </attempt>
     *     <attempt>...</attempt>
     * </retry>
     */
    private List<RetryAttempt> getRetryPerAttemptRetries(final String destinationUrl,
                                                         final List<WebhookRetryAttempt> retryAttemptsDto) {
        List<RetryAttempt> attempts = new ArrayList<>(retryAttemptsDto.size());
        for (WebhookRetryAttempt attempt : retryAttemptsDto) {
            attempts.add(buildAttempt(
                    attempt.getReceive().getExpected(),
                    attempt.getResponse(),
                    attempt.getReceive().isForward(),
                    destinationUrl
            ));
        }
        return attempts;
    }

    /**
     * The method is responsible for building {@link RetryAttempt}s based on the single template:
     * <retry min="1" max="3">
     *     <receive ... />
     *     <response ... />
     * </retry>
     */
    private List<RetryAttempt> getTemplateRetries(final String destinationUrl,
                                                  final WebhookRetry retry,
                                                  final List<WebhookRetryAttempt> retryAttemptsDto) {
        int max = retry.getMax() != null ? retry.getMax() : 1;
        if (max < 1) {
            throw new DefaultFrameworkException("<retry max=\"" + max + "\"> must be greater than 0)");
        }
        RetryAttempt templateAttempt = buildAttempt(
                retry.getReceive().getExpected(),
                retry.getResponse(),
                retry.getReceive().isForward(),
                destinationUrl);
        List<RetryAttempt> attempts = new ArrayList<>(retryAttemptsDto.size());
        for (int i = 0; i < max; i++) {
            attempts.add(templateAttempt);
        }
        return attempts;
    }

    private Duration parseTimeout(final Integer timeout) {
        if (timeout < 0) {
            throw new DefaultFrameworkException("Timeout cannot be negative");
        }
        return Duration.ofSeconds(timeout);
    }

}
