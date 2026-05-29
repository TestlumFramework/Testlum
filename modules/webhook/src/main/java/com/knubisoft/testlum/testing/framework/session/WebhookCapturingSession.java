package com.knubisoft.testlum.testing.framework.session;

import com.knubisoft.testlum.testing.framework.retry.RetryAttempt;
import com.knubisoft.testlum.testing.framework.retry.RetryPlan;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeOutput;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.InboundSession;
import com.knubisoft.testlum.testing.framework.inbound.server.util.InboundResponseFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WebhookCapturingSession implements InboundSession {

    private final String endpoint;
    private final RetryPlan retryPlan;
    private final ScheduledExecutorService scheduler;

    private final CompletableFuture<WebhookExchange> exchangeTask = new CompletableFuture<>();
    private final Object lock = new Object();

    private WebhookSessionPhase phase = WebhookSessionPhase.AWAITING_INITIAL;
    private boolean terminated = false;
    private ScheduledFuture<?> currentDeadline; // in which waiting state we're now - overall/idle/guard
    private final List<InboundRequest> receivedRequests = new ArrayList<>();
    private final List<InboundResponse> responsesSendToSender = new ArrayList<>();
    private final List<InboundResponse> responsesFromDestination = new ArrayList<>();
    private int retriesSeen = 0; // without initial request

    public WebhookCapturingSession(String endpoint, RetryPlan retryPlan, ScheduledExecutorService scheduler) {
        this.endpoint = endpoint;
        this.retryPlan = retryPlan;
        this.scheduler = scheduler;
    }

    @Override
    public String endpoint() {
        return endpoint;
    }

    @Override
    public WebhookExchange awaitExchange() {
        synchronized (lock) {
            scheduleDeadline(retryPlan.getOverallTimeout(), this::onOverallTimeout);
        }
        try {
            return exchangeTask.get(retryPlan.getOverallTimeout().toMillis() + 5000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return forceTerminate(WebhookExchange.timeout());
        } catch (ExecutionException e) {
            return WebhookExchange.error(null, "session failed: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return WebhookExchange.error(null, "session interrupted");
        }
    }

    @Override
    public InboundResponse onRequest(InboundRequest request) {
        synchronized (lock) {
            if (terminated) {
                // Log that session has been terminated
                return InboundResponseFactory.json(409, "{\"error\":\"webhook session already completed\"}");
            }

            switch (phase) {
                case AWAITING_INITIAL -> {
                    return handleInitial(request);
                }
                case AWAITING_RETRIES -> {
                    return handleRetry(request);
                }
                case GUARD -> {
                    return handleGuardViolation(request);
                }
                default -> {
                    throw new DefaultFrameworkException("Unexpected phase: " + phase);
                }
            }
        }
    }

    private InboundResponse handleInitial(InboundRequest request) {
        InboundExchangeOutput outcome;
        try {
            outcome = retryPlan.getInitialAttempt().getProducer().produce(request);
        } catch (Exception e) {
            return completeAsError(request, e);
        }
        captureExchange(request, outcome);

        if (retryPlan.getMaxRetries() == 0) {
            return transitionToGuardOrFinish(outcome);
        }
        phase = WebhookSessionPhase.AWAITING_RETRIES;
        scheduleDeadline(retryPlan.getIdleTimeout(), this::onIdleTimeout);
        return outcome.getResponseToSender();
    }

    private InboundResponse handleRetry(InboundRequest request) {
        if (retriesSeen >= retryPlan.getMaxRetries()) {
            return finishWithUnexpectedExtraRetry(request);
        }
        RetryAttempt attempt = retryPlan.getRetryAttempts().get(retriesSeen);
        InboundExchangeOutput outcome;
        try {
            outcome = attempt.getProducer().produce(request);
        } catch (Exception e) {
            return completeAsError(request, e);
        }
        retriesSeen++;
        captureExchange(request, outcome);

        if (retriesSeen >= retryPlan.getMaxRetries()) {
            return transitionToGuardOrFinish(outcome);
        }
        scheduleDeadline(retryPlan.getIdleTimeout(), this::onIdleTimeout);
        return outcome.getResponseToSender();
    }

    // This phase means that all expected retries have arrived. It results in immediate success
    // if guard = 0, or switching into guard processing (noRetry or maxRetries).
    private InboundResponse transitionToGuardOrFinish(InboundExchangeOutput lastOutcome) {
        if (retryPlan.getNoRetryTimeout().isZero() || retryPlan.getNoRetryTimeout().isNegative()) {
            finishSuccess();
            return lastOutcome.getResponseToSender();
        }
        phase = WebhookSessionPhase.GUARD;
        scheduleDeadline(retryPlan.getNoRetryTimeout(), this::onGuardExpired);
        return lastOutcome.getResponseToSender();
    }

    // Means that unexpected retry has arrived.
    private InboundResponse handleGuardViolation(InboundRequest request) {
        return finishWithUnexpectedExtraRetry(request);
    }

    private void finishSuccess() {
        cancelDeadline();
        terminated = true;
        exchangeTask.complete(WebhookExchange.success(receivedRequests, responsesSendToSender, responsesFromDestination));
    }

    private InboundResponse finishWithUnexpectedExtraRetry(InboundRequest extraRequest) {
        cancelDeadline();
        terminated = true;
        exchangeTask.complete(WebhookExchange
                .unexpectedExtraRequest(receivedRequests, responsesSendToSender, responsesFromDestination, extraRequest));
        return InboundResponseFactory.json(409,  "{\"error\":\"webhook session received unexpected extra call\"}");
    }


    private InboundResponse completeAsError(InboundRequest request, Exception e) {
        cancelDeadline();
        terminated = true;
        exchangeTask.complete(WebhookExchange.error(request, e.getMessage()));
        return InboundResponseFactory.internalError("webhook processing failed: " + e.getMessage());
    }

    private void onOverallTimeout() {
        synchronized (lock) {
            if (terminated) {
                return;
            }
            terminated = true;
            exchangeTask.complete(receivedRequests.isEmpty() ?
                    WebhookExchange.timeout() :
                    WebhookExchange.success(receivedRequests, responsesSendToSender, responsesFromDestination));
            /*
             * Here is the logic: if some retry has arrived, it's not timeout but success.
             * Reason: because idle/guard handler has already completed the guard logic (noRetry, min/max retries)
             */
        }
    }

    private void onIdleTimeout() {
        synchronized (lock) {
            if (terminated || phase != WebhookSessionPhase.AWAITING_RETRIES) {
                return;
            }
            // sender hasn't sent new retry
            // min retries have not been reached
            if (retriesSeen < retryPlan.getMinRetries()) {
                terminated = true;
                exchangeTask.complete(WebhookExchange.belowMin(receivedRequests, responsesSendToSender,
                        responsesFromDestination, retryPlan.getMinRetries(), retriesSeen));
                return;
            }
            // min retries have been reached
            if (retryPlan.getNoRetryTimeout().isZero()) {
                finishSuccess();
                return;
            }
            phase = WebhookSessionPhase.GUARD;
            scheduleDeadline(retryPlan.getNoRetryTimeout(), this::onGuardExpired);
        }
    }

    private void onGuardExpired() {
        synchronized (lock) {
            if (terminated || phase != WebhookSessionPhase.GUARD) {
                return;
            }
            finishSuccess();
        }
    }

    // Utilities (they're called under lock)
    private void captureExchange(InboundRequest request, InboundExchangeOutput outcome) {
        receivedRequests.add(request);
        responsesSendToSender.add(outcome.getResponseToSender());
        if (outcome.getResponseFromDestination() != null) {
            responsesFromDestination.add(outcome.getResponseFromDestination());
        }
    }

    private void scheduleDeadline(Duration delay, Runnable action) {
        cancelDeadline();
        if (delay == null || delay.isZero() || delay.isNegative()) {
            action.run();
            return;
        }
        currentDeadline = scheduler.schedule(action, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void cancelDeadline() {
       if (currentDeadline != null) {
           currentDeadline.cancel(false);
           currentDeadline = null;
       }
    }

    private WebhookExchange forceTerminate(WebhookExchange fallback) {
        synchronized (lock) {
            if (!terminated) {
                terminated = true;
                cancelDeadline();
                exchangeTask.complete(fallback);
            }
        }
        return exchangeTask.getNow(fallback);
    }


    @Override
    public boolean isCompleted() {
        return exchangeTask.isDone();
    }
}
