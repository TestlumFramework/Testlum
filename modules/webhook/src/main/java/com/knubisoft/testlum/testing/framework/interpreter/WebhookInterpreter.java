package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testing.framework.producer.ResponseProducer;
import com.knubisoft.testing.framework.producer.ResponseProducerFactory;
import com.knubisoft.testing.framework.session.SessionEvent;
import com.knubisoft.testing.framework.session.SessionListener;
import com.knubisoft.testlum.testing.framework.coordinator.WebhookCoordinator;
import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.coordinator.WebhookExecutorProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeOutput;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher;
import com.knubisoft.testlum.testing.framework.inbound.server.util.InboundResponseFactory;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.HttpValidator;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.retry.RetryAttempt;
import com.knubisoft.testlum.testing.framework.retry.RetryPlan;
import com.knubisoft.testlum.testing.framework.session.WebhookSessionListener;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.framework.util.WebhookInterpreterUtil;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.scenario.Webhook;
import com.knubisoft.testlum.testing.model.scenario.WebhookExpected;
import com.knubisoft.testlum.testing.model.scenario.WebhookHeader;
import com.knubisoft.testlum.testing.model.scenario.WebhookMatcher;
import com.knubisoft.testlum.testing.model.scenario.WebhookMatcherEndpoint;
import com.knubisoft.testlum.testing.model.scenario.WebhookMatcherJson;
import com.knubisoft.testlum.testing.model.scenario.WebhookMatcherNameValue;
import com.knubisoft.testlum.testing.model.scenario.WebhookMockReturn;
import com.knubisoft.testlum.testing.model.scenario.WebhookReceive;
import com.knubisoft.testlum.testing.model.scenario.WebhookResponse;
import com.knubisoft.testlum.testing.model.scenario.WebhookRetry;
import com.knubisoft.testlum.testing.model.scenario.WebhookRetryAttempt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.knubisoft.testlum.testing.framework.util.WebhookInterpreterUtil.nullSafeList;

@Slf4j
@InterpreterForClass(Webhook.class)
public class WebhookInterpreter extends AbstractInterpreter<Webhook> {

    private static final String STEP = "Step";
    private final WebhookCoordinator coordinator;
    private final ResponseProducerFactory producerFactory;
    private final IntegrationsProvider integrationsProvider;

    public WebhookInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.coordinator = dependencies.getContext().getBean(WebhookCoordinator.class);
        this.producerFactory = dependencies.getContext().getBean(ResponseProducerFactory.class);
        this.integrationsProvider = dependencies.getContext().getBean(IntegrationsProvider.class);
    }

    @Override
    protected void acceptImpl(Webhook o, CommandResult result) {
        Webhook webhook = injectCommand(o);

        if (webhook.getPath() == null) {
            throw new DefaultFrameworkException("Webhook path is required");
        }

        WebhookReceive receive = webhook.getReceive();
        ensureAlias(receive::getAlias, receive::setAlias);

        String url = integrationsProvider.findListByEnv(Api.class, dependencies.getEnvironment())
                .stream().filter(i -> i.getAlias().equals(receive.getAlias()))
                .findFirst().orElseThrow(() -> new DefaultFrameworkException(
                        "API configuration with alias " + receive.getAlias() + " not found"))
                .getUrl();

        String destinationUrl = url + webhook.getPath();

        logWebhookInfo(receive.getAlias(), destinationUrl, receive.isForward());
        addWebhookMetaData(receive.getAlias(), destinationUrl, receive.isForward(), result);

        RetryPlan retryPlan = buildRetryPlan(webhook, destinationUrl);

        CorrelationMatcher matcher = buildMatcher(webhook);

        List<CommandResult> subCommandResults = new ArrayList<>();
        result.setSubCommandsResult(subCommandResults);

        BlockingQueue<SessionEvent> events = new LinkedBlockingQueue<>();
        SessionListener listener = new WebhookSessionListener(events);

        Future<?> sessionFuture = dependencies.getContext().getBean(WebhookExecutorProvider.class)
            .executor().submit(() ->
                coordinator.awaitOneRequest(webhook.getPath(), currentTestId(), matcher, retryPlan, listener));

        Throwable terminalError = null;
        try {
            consumeEvents(events, retryPlan, webhook, receive, subCommandResults);
        } catch (Throwable t) {
            terminalError = t;
        } finally {
            try {
                sessionFuture.get(2, TimeUnit.SECONDS);
            } catch (Exception ignored) {

            }
        }

        if (terminalError != null) {
            Exception exception = terminalError instanceof Exception e
                    ? e : new DefaultFrameworkException(List.of(terminalError.getMessage()));
            setExceptionResult(result, exception);
            checkIfStopScenarioOnFailure(exception);
        }
    }

    private void consumeEvents(BlockingQueue<SessionEvent> events, RetryPlan plan, Webhook webhook, WebhookReceive receive,
                               List<CommandResult> subCommandResults) throws Throwable {
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
            if (ev instanceof SessionEvent.Received r) {
                RetryAttempt attempt = (r.index() == 0)
                        ? plan.getInitialAttempt()
                        : plan.getRetryAttempts().get(r.index() - 1);
                emitReceiveCommand(receive.getAlias(), r.index(), plan.getMaxRetries(),
                        attempt, r.request(), subCommandResults);
                Object expectedDestination = attempt.getExpectedDestinationResponse();
                if (expectedDestination instanceof WebhookExpected exp && r.output().getResponseFromDestination() != null) {
                    emitResponseCommand(receive.getAlias(), r.index(), plan.getMaxRetries(),
                            exp, r.output().getResponseFromDestination(), subCommandResults);
                }
            } else if (ev instanceof SessionEvent.Completed) {
                return;
            } else if (ev instanceof SessionEvent.Failed f) {
                throw new DefaultFrameworkException(List.of(WebhookInterpreterUtil.failureMessage(receive, webhook, f)));
            }
        }
    }

    private void emitReceiveCommand(String alias, int index, int maxRetries, RetryAttempt attempt,
                                    InboundRequest request, List<CommandResult> subCommandResults) {
        String stepName = (index == 0) ? "receive" : "retry receive #" + index;
        log.info(LogFormat.commandLog(), dependencies.getPosition().incrementAndGet(), stepName);
        CommandResult subResult = newCommandResultInstance(dependencies.getPosition().get());
        subCommandResults.add(subResult);

        StopWatch stopWatch = StopWatch.createStarted();
        log.info(ALIAS_LOG, alias);
        subResult.put(ALIAS, alias);
        subResult.put(STEP, stepName);
        try {
            HttpValidator httpValidator = new HttpValidator(this, stringPrettifier);
            validateReceivedBody(attempt.getExpectedRequestFile(), request, httpValidator, subResult);
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

    private void emitResponseCommand(String alias, int index, int maxRetries, WebhookExpected expected,
                                     InboundResponse actual, List<CommandResult> commandResultList) {
        String stepName = (index == 0) ? "response" : "retry response #" + index;
        log.info(LogFormat.commandLog(), dependencies.getPosition().incrementAndGet(), stepName);
        CommandResult subResult = newCommandResultInstance(dependencies.getPosition().get());
        commandResultList.add(subResult);

        StopWatch stopWatch = StopWatch.createStarted();
        log.info(ALIAS_LOG, alias);
        subResult.put(ALIAS, alias);
        subResult.put(STEP, stepName);
        try {
            HttpValidator httpValidator = new HttpValidator(this, stringPrettifier);
            validateResponseFromDestination(expected, actual, httpValidator, subResult);
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

    private CorrelationMatcher buildMatcher(Webhook webhook) {
        WebhookMatcher matcherStub = webhook.getWebhookMatcher();
        if (matcherStub == null) {
            return CorrelationMatcher.always();
        }

        List<WebhookMatcherNameValue> headers = nullSafeList(matcherStub.getByHeader());
        List<WebhookMatcherJson> jsons = nullSafeList(matcherStub.getByJson());
        List<WebhookMatcherNameValue> qParams = nullSafeList(matcherStub.getByQueryParam());
        List<WebhookMatcherEndpoint> paths = nullSafeList(matcherStub.getByEndpoint());
        boolean always = matcherStub.isAlways();

        int otherCount = headers.size() + jsons.size() + qParams.size() + paths.size();
        if (otherCount > 0 && always) {
            throw new DefaultFrameworkException("<always>=\"false\" cannot be combined with other criteria");
        }
        if (otherCount == 0 || always) {
            return CorrelationMatcher.always();
        }

        List<CorrelationMatcher> matchers = new ArrayList<>(otherCount);
        for (WebhookMatcherNameValue header : headers) {
            matchers.add(CorrelationMatcher.header(header.getName(), header.getValue(), false));
        }
        for (WebhookMatcherJson json : jsons) {
            matchers.add(CorrelationMatcher.jsonPath(json.getJpath(), json.getValue()));
        }
        for (WebhookMatcherNameValue qParam : qParams) {
            matchers.add(CorrelationMatcher.queryParam(qParam.getName(), qParam.getValue(), false));
        }
        for (WebhookMatcherEndpoint path : paths) {
            matchers.add(CorrelationMatcher.endpoint(path.getUrl()));
        }
        return CorrelationMatcher.combined(matchers);
    }

    private RetryPlan buildRetryPlan(Webhook webhook, String destinationUrl) {
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

    private Duration parseTimeout(Integer timeout) {
        if (timeout < 0) {
            throw new DefaultFrameworkException("Timeout cannot be negative");
        }
        return Duration.ofSeconds(timeout);
    }

    private List<RetryAttempt> buildRetries(Webhook webhook, String destinationUrl) {
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

        if (commonRetryTemplate) {
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
        return List.of();
    }

    private RetryAttempt buildAttempt(String expectedFile, WebhookResponse response,
                                      Boolean forward, String destinationUrl) {
        ResponseProducer producer = buildResponseProducer(forward, response, destinationUrl);
        WebhookExpected expectedDestination = (response != null) ? response.getExpected() : null;
        return new RetryAttempt(expectedFile, expectedDestination, producer);
    }

    private void validateReceivedBody(String expectedFile, InboundRequest request, HttpValidator validator,
                                      CommandResult result) {
        String expectedBody = getContentIfFile(expectedFile);
        if (StringUtils.isNotBlank(expectedBody)) {
            String actualBody = new String(request.getBody(), StandardCharsets.UTF_8);
            setContextBody(getContextBodyKey(expectedFile), actualBody);
            /**
             * Show only last retry attempt - it's the most important one.
             * If every attempt has succeeded, then the last one is the most important one.
             * If something went wrong, HttpValidator will show all attempts at the end.
             */
            result.setActual(stringPrettifier.asJsonResult(actualBody));
            result.setExpected(stringPrettifier.asJsonResult(expectedBody));
            validator.validateBody(expectedBody, actualBody);
        }
    }

    private void validateResponseFromDestination(WebhookExpected expected, InboundResponse actual,
                                                 HttpValidator validator, CommandResult result) {
        if (expected.getCode() != null) {
            validator.validateCode(expected.getCode(), actual.getStatus());
        }

        if (expected.getHeaders() != null && !expected.getHeaders().getHeader().isEmpty()) {
            Map<String, String> expectedHeaders = new LinkedHashMap<>();
            for (WebhookHeader header : expected.getHeaders().getHeader()) {
                expectedHeaders.put(header.getName(), header.getValue());
            }
            Map<String, String> actualHeaders = firstValueHeader(actual.getHeaders());
            validator.validateHeaders(expectedHeaders, actualHeaders);
        }

        String expectedBody = getContentIfFile(expected.getFile());
        if (StringUtils.isNotBlank(expectedBody)) {
            String actualBody = new String(actual.getBody(), StandardCharsets.UTF_8);
            setContextBody(getContextBodyKey(expected.getFile()), actualBody);
            result.setActual(stringPrettifier.asJsonResult(actualBody));
            result.setExpected(stringPrettifier.asJsonResult(expectedBody));
            validator.validateBody(expectedBody, actualBody);
        }
    }

    private Map<String, String> firstValueHeader(Map<String, List<String>> headers) {
        Map<String, String> flatHeaders = new LinkedHashMap<>();
        headers.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                flatHeaders.put(key, value.get(0));
            }
        });
        return flatHeaders;
    }

    private ResponseProducer buildResponseProducer(Boolean forward, WebhookResponse response,
                                                   String destinationUrl) {

        if (forward) {
            if (StringUtils.isBlank(destinationUrl)) {
                throw new DefaultFrameworkException("forward=\"true\" requires destinationUrl");
            }
            if (response != null && response.getMockReturn() != null) {
                InboundResponse modifiedResponse = buildMockResponse(response);
                return producerFactory.modifiedProducer(destinationUrl, modifiedResponse);
            }
            return producerFactory.forwardingProducer(destinationUrl);
        }

        InboundResponse mockResponse = buildMockResponse(response);
        return request -> InboundExchangeOutput.mock(mockResponse);
    }

    private InboundResponse buildMockResponse(WebhookResponse response) {
        WebhookMockReturn mockResult = response != null ? response.getMockReturn() : null;
        if (mockResult == null) {
            // Log warning
            return InboundResponseFactory.of(HttpStatus.NO_CONTENT.value(), Map.of(), new byte[0]);
        }
        Integer status = mockResult.getStatus() == null ? HttpStatus.NO_CONTENT.value() : mockResult.getStatus();
        byte[] body = new byte[0];
        String bodyContent = getContentIfFile(mockResult.getFile());
        if (StringUtils.isNotBlank(bodyContent)) {
            body = bodyContent.getBytes(StandardCharsets.UTF_8);
        }
        Map<String, List<String>> headers = new LinkedHashMap<>();
        if (mockResult.getHeaders() != null && !mockResult.getHeaders().getHeader().isEmpty()) {
            for (WebhookHeader header : mockResult.getHeaders().getHeader()) {
                headers.computeIfAbsent(header.getName(), k -> new ArrayList<>()).add(header.getValue());
            }
        }
        return InboundResponseFactory.of(status, headers, body);
    }

    // TODO: just a stub for now
    private String currentTestId() {
        return Thread.currentThread().getName();
    }
    private void logWebhookInfo(String alias, String endpoint, Boolean forward) {
        log.info(ALIAS_LOG, alias);
        log.info(ENDPOINT_LOG, endpoint);
        log.info(LogFormat.table("Forward", forward));
    }
    private void addWebhookMetaData(String alias, String endpoint, Boolean forward, CommandResult result) {
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
