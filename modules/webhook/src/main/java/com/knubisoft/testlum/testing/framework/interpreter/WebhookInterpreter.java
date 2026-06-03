package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testing.framework.producer.ResponseProducerFactory;
import com.knubisoft.testing.framework.session.SessionEvent;
import com.knubisoft.testlum.testing.framework.coordinator.WebhookCoordinator;
import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.coordinator.WebhookExecutorProvider;
import com.knubisoft.testlum.testing.framework.coordinator.WebhookMatcherBuilder;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.HttpValidator;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.retry.RetryAttempt;
import com.knubisoft.testlum.testing.framework.retry.RetryPlan;
import com.knubisoft.testlum.testing.framework.retry.RetryPlanFactory;
import com.knubisoft.testlum.testing.framework.session.WebhookSessionListener;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.framework.util.WebhookInterpreterUtil;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.scenario.Webhook;
import com.knubisoft.testlum.testing.model.scenario.WebhookExpected;
import com.knubisoft.testlum.testing.model.scenario.WebhookReceive;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@InterpreterForClass(Webhook.class)
public class WebhookInterpreter extends AbstractInterpreter<Webhook> {

    private static final String STEP = "Step";
    private final WebhookCoordinator coordinator;
    private final ResponseProducerFactory producerFactory;
    private final IntegrationsProvider integrationsProvider;
    private final RetryPlanFactory retryPlanFactory;
    private final WebhookCommandValidator webhookCommandValidator;

    public WebhookInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.coordinator = dependencies.getContext().getBean(WebhookCoordinator.class);
        this.producerFactory = dependencies.getContext().getBean(ResponseProducerFactory.class);
        this.integrationsProvider = dependencies.getContext().getBean(IntegrationsProvider.class);
        this.retryPlanFactory = new RetryPlanFactory(contentResolver, producerFactory);
        this.webhookCommandValidator = new WebhookCommandValidator(contentResolver, stringPrettifier, dependencies);
    }

    @Override
    protected void acceptImpl(final Webhook o, final CommandResult result) {
        Webhook webhook = injectCommand(o);
        if (webhook.getPath() == null) {
            throw new DefaultFrameworkException("Webhook path is required");
        }
        WebhookReceive receive = webhook.getReceive();
        ensureAlias(receive::getAlias, receive::setAlias);

        String destinationUrl = getDestinationUrlByAlias(receive, webhook);
        logWebhookInfo(receive.getAlias(), destinationUrl, receive.isForward());
        addWebhookMetaData(receive.getAlias(), destinationUrl, receive.isForward(), result);
        executeWebhookCommand(result, webhook, destinationUrl);
    }

    private void executeWebhookCommand(final CommandResult result,
                                       final Webhook webhook,
                                       final String destinationUrl) {
        RetryPlan retryPlan = retryPlanFactory.buildRetryPlan(webhook, destinationUrl);
        BlockingQueue<SessionEvent> sessionEvents = new LinkedBlockingQueue<>();

        Future<?> sessionFuture = dependencies.getContext().getBean(WebhookExecutorProvider.class)
            .executor().submit(awaitOneRequest(webhook, retryPlan, sessionEvents));

        List<CommandResult> subCommandResults = new ArrayList<>();
        result.setSubCommandsResult(subCommandResults);

        Throwable terminalError = null;
        try {
            consumeEvents(sessionEvents, retryPlan, webhook, subCommandResults);
        } catch (Throwable t) {
            terminalError = t;
        } finally {
            try {
                sessionFuture.get(2, TimeUnit.SECONDS);
            } catch (Exception ignored) {

            }
        }
        if (terminalError != null) {
            handleTerminalError(result, terminalError);
        }
    }

    private Callable<?> awaitOneRequest(final Webhook webhook,
                                        final RetryPlan retryPlan,
                                        final BlockingQueue<SessionEvent> sessionEvents) {
        CorrelationMatcher matcher = WebhookMatcherBuilder.buildMatcher(webhook);
        WebhookSessionListener sessionListener = new WebhookSessionListener(sessionEvents);
        return () -> coordinator.awaitOneRequest(
                webhook.getPath(), currentTestId(),
                matcher,
                retryPlan, sessionListener
        );
    }

    private void consumeEvents(final BlockingQueue<SessionEvent> events,
                               final RetryPlan plan,
                               final Webhook webhook,
                               final List<CommandResult> subCommandResults) throws Throwable {
        WebhookReceive receive = webhook.getReceive();
        long deadlineNanos = System.nanoTime() + plan.getOverallTimeout().plusSeconds(10).toNanos();
        while (true) {
            long remaining = deadlineNanos - System.nanoTime();
            if (remaining <= 0) {
                throw new DefaultFrameworkException("Webhook event stream didn't terminate within " + plan.getOverallTimeout());
            }
            SessionEvent ev = events.poll(remaining, TimeUnit.NANOSECONDS);
            if (ev == null) {
                continue;
            }
            if (ev instanceof SessionEvent.Received received) {
                processReceiveEvent(plan, subCommandResults, received, receive);
            } else if (ev instanceof SessionEvent.Completed) {
                return;
            } else if (ev instanceof SessionEvent.Failed f) {
                throw new DefaultFrameworkException(List.of(WebhookInterpreterUtil.failureMessage(receive, webhook, f)));
            }
        }
    }

    private void processReceiveEvent(final RetryPlan plan,
                                     final List<CommandResult> subCommandResults,
                                     final SessionEvent.Received event,
                                     final WebhookReceive receive) {
        RetryAttempt attempt = (event.index() == 0)
                ? plan.getInitialAttempt()
                : plan.getRetryAttempts().get(event.index() - 1);
        emitReceiveCommand(receive.getAlias(), event.index(),
                attempt, event.request(), subCommandResults);
        Object expectedDestination = attempt.getExpectedDestinationResponse();
        if (expectedDestination instanceof WebhookExpected exp && event.output().getResponseFromDestination() != null) {
            emitResponseCommand(receive.getAlias(), event.index(),
                    exp, event.output().getResponseFromDestination(), subCommandResults);
        }
    }

    private void emitReceiveCommand(final String alias, final int index,
                                    final RetryAttempt attempt, final InboundRequest request,
                                    final List<CommandResult> subCommandResults) {
        String stepName = (index == 0) ? "receive" : "retry receive #" + index;
        emitWebhookCommand(alias, stepName, subCommandResults,
                webhookCommandValidator.receiveBodyValidator(attempt, request));
    }

    private void emitResponseCommand(final String alias, final int index,
                                     final WebhookExpected expected, final InboundResponse actual,
                                     final List<CommandResult> commandResultList) {
        String stepName = (index == 0) ? "response" : "retry response #" + index;
        emitWebhookCommand(alias, stepName, commandResultList,
                webhookCommandValidator.responseFromDestinationValidator(expected, actual));
    }

    private void emitWebhookCommand(final String alias, final String stepName,
                                    final List<CommandResult> subCommandResults,
                                    final ValidationAction validationAction) {
        log.info(LogFormat.commandLog(), dependencies.getPosition().incrementAndGet(), stepName);
        CommandResult subResult = newCommandResultInstance(dependencies.getPosition().get());
        subCommandResults.add(subResult);

        StopWatch stopWatch = StopWatch.createStarted();
        log.info(ALIAS_LOG, alias);
        subResult.put(ALIAS, alias);
        subResult.put(STEP, stepName);
        try {
            HttpValidator httpValidator = new HttpValidator(this, stringPrettifier);
            validationAction.validate(httpValidator, subResult);
            httpValidator.rethrowOnErrors();
        } catch (Exception e) {
            logException(e);
            setExceptionResult(subResult, e);
            checkIfStopScenarioOnFailure(e);
        } finally {
            subResult.setExecutionTime(stopWatch.getDuration().toMillis());
            stopWatch.stop();
        }
    }

    private String getDestinationUrlByAlias(final WebhookReceive receive, final Webhook webhook) {
        String integrationUrl = integrationsProvider.findListByEnv(Api.class, dependencies.getEnvironment())
                .stream().filter(i -> i.getAlias().equals(receive.getAlias()))
                .findFirst().orElseThrow(() -> new DefaultFrameworkException(
                        "API configuration with alias " + receive.getAlias() + " not found"))
                .getUrl();
        return integrationUrl.concat(webhook.getPath());
    }

    private void handleTerminalError(final CommandResult result, final Throwable terminalError) {
        Exception exception = terminalError instanceof Exception e
                ? e : new DefaultFrameworkException(List.of(terminalError.getMessage()));
        setExceptionResult(result, exception);
        checkIfStopScenarioOnFailure(exception);
    }

    private String currentTestId() {
        return Thread.currentThread().getName();
    }

    private void logWebhookInfo(final String alias,
                                final String endpoint,
                                final Boolean forward) {
        log.info(ALIAS_LOG, alias);
        log.info(ENDPOINT_LOG, endpoint);
        log.info(LogFormat.table("Forward", forward));
    }
    private void addWebhookMetaData(final String alias,
                                    final String endpoint,
                                    final Boolean forward,
                                    final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ENDPOINT_LOG, endpoint);
        result.put(LogFormat.table("Forward"), forward);
    }

    protected CommandResult newCommandResultInstance(final int number) {
        CommandResult commandResult = new CommandResult();
        commandResult.setId(number);
        commandResult.setSuccess(true);
        return commandResult;
    }
}
