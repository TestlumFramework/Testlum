package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testing.framework.producer.ResponseProducer;
import com.knubisoft.testing.framework.producer.ResponseProducerFactory;
import com.knubisoft.testlum.testing.framework.coordinator.WebhookCoordinator;
import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testing.framework.coordinator.InboundDestination;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeOutput;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeStatus;
import com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher;
import com.knubisoft.testlum.testing.framework.inbound.server.util.InboundResponseFactory;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.HttpValidator;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.retry.RetryAttempt;
import com.knubisoft.testlum.testing.framework.retry.RetryPlan;
import com.knubisoft.testlum.testing.framework.session.WebhookExchange;
import com.knubisoft.testlum.testing.framework.stub.Webhook;
import com.knubisoft.testlum.testing.framework.stub.WebhookMockResult;
import com.knubisoft.testlum.testing.framework.stub.WebhookReceive;
import com.knubisoft.testlum.testing.framework.stub.WebhookResponse;
import com.knubisoft.testlum.testing.framework.stub.WebhookResponseExpected;
import com.knubisoft.testlum.testing.framework.stub.WebhookRetry;
import com.knubisoft.testlum.testing.framework.stub.WebhookRetryAttempt;
import com.knubisoft.testlum.testing.framework.stub.matcher.ByHeader;
import com.knubisoft.testlum.testing.framework.stub.matcher.ByJson;
import com.knubisoft.testlum.testing.framework.stub.matcher.ByPath;
import com.knubisoft.testlum.testing.framework.stub.matcher.ByQueryParam;
import com.knubisoft.testlum.testing.framework.stub.matcher.WebhookMatcher;
import com.knubisoft.testlum.testing.model.scenario.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@InterpreterForClass(Webhook.class)
public class WebhookInterpreter extends AbstractInterpreter<Webhook> {

    private final WebhookCoordinator coordinator;
    private final ResponseProducerFactory producerFactory;

    protected WebhookInterpreter(InterpreterDependencies dependencies) {
        super(dependencies);
        this.coordinator = dependencies.getContext().getBean(WebhookCoordinator.class);
        this.producerFactory = dependencies.getContext().getBean(ResponseProducerFactory.class);
    }

    @Override
    protected void acceptImpl(Webhook o, CommandResult result) {
        Webhook webhook = injectCommand(o);
        WebhookReceive receive = webhook.getReceive();
        ensureAlias(receive::getAlias, receive::setAlias);

        InboundDestination destination = coordinator.resolve(receive.getAlias(), dependencies.getEnvironment());

        logWebhookInfo(receive.getAlias(), destination.getEndpoint(), receive.getForward());
        addWebhookMetaData(receive.getAlias(), destination.getEndpoint(), receive.getForward(), result);

        RetryPlan retryPlan = buildRetryPlan(webhook, destination);

        CorrelationMatcher matcher = buildMatcher(webhook);

        WebhookExchange exchange = coordinator.awaitOneRequest(
                destination.getEndpoint(), currentTestId(), matcher, retryPlan);

        compare(retryPlan, webhook, receive, exchange, result);
    }

    private CorrelationMatcher buildMatcher(Webhook webhook) {
        WebhookMatcher matcherStub = webhook.getWebhookMatcher();
        if (matcherStub == null) {
            return CorrelationMatcher.always();
        }

        List<ByHeader> headers = nullSafeList(matcherStub.getByHeader());
        List<ByJson> jsons = nullSafeList(matcherStub.getByJson());
        List<ByQueryParam> qParams = nullSafeList(matcherStub.getByQueryParam());
        List<ByPath> paths = nullSafeList(matcherStub.getByPath());
        boolean always = matcherStub.getAlways();

        int otherCount = headers.size() + jsons.size() + qParams.size() + paths.size();
        if (otherCount > 0 && always) {
            throw new DefaultFrameworkException("<always>=\"false\" cannot be combined with other criteria");
        }
        if (otherCount == 0 || always) {
            return CorrelationMatcher.always();
        }

        List<CorrelationMatcher> matchers = new ArrayList<>(otherCount);
        for (ByHeader header : headers) {
            matchers.add(CorrelationMatcher.header(header.getName(), header.getValue(), header.getContains()));
        }
        for (ByJson json : jsons) {
            matchers.add(CorrelationMatcher.jsonPath(json.getjPath(), json.getValue()));
        }
        for (ByQueryParam qParam : qParams) {
            matchers.add(CorrelationMatcher.queryParam(qParam.getName(), qParam.getValue(), qParam.getContains()));
        }
        for (ByPath path : paths) {
            matchers.add(CorrelationMatcher.endpoint(path.getUrl()));
        }
        return CorrelationMatcher.combined(matchers);
    }

    private RetryPlan buildRetryPlan(Webhook webhook, InboundDestination destination) {
        RetryAttempt initialRequest = buildAttempt(
                webhook.getReceive().getExpected(),
                webhook.getResponse(),
                webhook.getReceive().getForward(),
                destination);

        List<RetryAttempt> retryAttempts = buildRetries(webhook, destination);

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

    private List<RetryAttempt> buildRetries(Webhook webhook, InboundDestination destination) {
        WebhookRetry retry = webhook.getRetry();
        if (retry == null) {
            return List.of();
        }
        List<WebhookRetryAttempt> retryAttemptsDto = retry.getAttempts();
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
                        attempt.getReceive().getForward(),
                        destination
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
                    retry.getReceive().getForward(),
                    destination);
            List<RetryAttempt> attempts = new ArrayList<>(retryAttemptsDto.size());
            for (int i = 0; i < max; i++) {
                attempts.add(templateAttempt);
            }
            return attempts;
        }
        return List.of();
    }

    private RetryAttempt buildAttempt(String expectedFile, WebhookResponse response,
                                      Boolean forward, InboundDestination destination) {
        ResponseProducer producer = buildResponseProducer(forward, response, destination);
        WebhookResponseExpected expectedDestination = (response != null) ? response.getExpected() : null;
        return new RetryAttempt(expectedFile, expectedDestination, producer);
    }

    private void compare(RetryPlan plan, Webhook webhook, WebhookReceive receive, WebhookExchange exchange, CommandResult result) {
        if (exchange.getStatus() == InboundExchangeStatus.TIMEOUT) {
            throw new DefaultFrameworkException("No webhook received within " + webhook.getTimeout() + " seconds");
        }
        if (exchange.getStatus() == InboundExchangeStatus.ERROR) {
            throw new DefaultFrameworkException("Error processing webhook: " + exchange.getErrorMessage());
        }
        if (exchange.getStatus() == InboundExchangeStatus.BELOW_MIN_RETRIES) {
            throw new DefaultFrameworkException("Webhook retry failed: " + exchange.getErrorMessage());
        }
        if (exchange.getStatus() == InboundExchangeStatus.UNEXPECTED_EXTRA_RETRY) {
            throw new DefaultFrameworkException(
                    "Receive unexpected extra webhook retry after completion: " + exchange.getErrorMessage());
        }
        HttpValidator httpValidator = new HttpValidator(this, stringPrettifier);

        List<InboundRequest> receivedRequests = exchange.getReceivedRequest();
        List<InboundResponse> responseFromDestination = exchange.getResponseFromDestination();

        for (int i = 0; i < receivedRequests.size(); i++) {
            RetryAttempt attempt = (i == 0) ? plan.getInitialAttempt() : plan.getRetryAttempts().get(i - 1);

            // Validation of the webhook payload
            validateReceivedBody(attempt.getExpectedRequestFile(), receivedRequests.get(i),
                                    httpValidator, result);

            Object expectedDestinationResponse = attempt.getExpectedDestinationResponse();
            if (expectedDestinationResponse instanceof WebhookResponseExpected response
                    && i < responseFromDestination.size()) {
                validateResponseFromDestination(response, responseFromDestination.get(i), httpValidator, result);
            }
        }
        httpValidator.rethrowOnErrors();
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

    private void validateResponseFromDestination(WebhookResponseExpected expected, InboundResponse actual,
                                                 HttpValidator validator, CommandResult result) {
        validator.validateCode(expected.getStatus(), actual.getStatus());

        if (expected.getHeader() != null && !expected.getHeader().isEmpty()) {
            Map<String, String> expectedHeaders = new LinkedHashMap<>();
            for (Header header : expected.getHeader()) {
                expectedHeaders.put(header.getName(), header.getData());
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
                                                   InboundDestination destination) {

        if (forward) {
            String destinationUrl = destination.getEndpoint();
            if (StringUtils.isBlank(destinationUrl)) {
                throw new DefaultFrameworkException("forward=\"true\" requires destinationUrl for alias " + destination.getAlias());
            }
            if (response != null && response.getMockResult() != null) {
                InboundResponse modifiedResponse = buildMockResponse(response);
                return producerFactory.modifiedProducer(destinationUrl, modifiedResponse);
            }
            return producerFactory.forwardingProducer(destinationUrl);
        }

        InboundResponse mockResponse = buildMockResponse(response);
        return request -> InboundExchangeOutput.mock(mockResponse);
    }

    private InboundResponse buildMockResponse(WebhookResponse response) {
        WebhookMockResult mockResult = response != null ? response.getMockResult() : null;
        if (mockResult == null) {
            // Log warning
            return InboundResponseFactory.of(HttpStatus.NO_CONTENT.value(), Map.of(), new byte[0]);
        }
        Integer status = mockResult.getStatus();
        byte[] body = new byte[0];
        String bodyContent = getContentIfFile(mockResult.getFile());
        if (StringUtils.isNotBlank(bodyContent)) {
            body = bodyContent.getBytes(StandardCharsets.UTF_8);
        }
        Map<String, List<String>> headers = new LinkedHashMap<>();
        if (mockResult.getHeader() != null) {
            for (Header header : mockResult.getHeader()) {
                headers.computeIfAbsent(header.getName(), k -> new ArrayList<>()).add(header.getData());
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

    private static <T> List<T> nullSafeList(List<T> list) {
        return list != null ? list : List.of();
    }
}
