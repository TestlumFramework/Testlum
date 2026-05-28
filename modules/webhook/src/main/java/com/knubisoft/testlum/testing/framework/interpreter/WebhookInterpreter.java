package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testing.framework.exchange.producer.ResponseProducer;
import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testing.framework.exchange.coordinator.HttpInboundCoordinator;
import com.knubisoft.testing.framework.exchange.coordinator.InboundDestination;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeResponseResult;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeStatus;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchange;
import com.knubisoft.testlum.testing.framework.inbound.server.util.CorrelationMatcher;
import com.knubisoft.testlum.testing.framework.inbound.server.util.InboundResponseFactory;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.HttpValidator;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.stub.Webhook;
import com.knubisoft.testlum.testing.framework.stub.WebhookMockResult;
import com.knubisoft.testlum.testing.framework.stub.WebhookReceive;
import com.knubisoft.testlum.testing.framework.stub.WebhookResponse;
import com.knubisoft.testlum.testing.framework.stub.WebhookResponseExpected;
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

    private final HttpInboundCoordinator coordinator;

    protected WebhookInterpreter(InterpreterDependencies dependencies) {
        super(dependencies);
        this.coordinator = dependencies.getContext().getBean(HttpInboundCoordinator.class);
    }

    @Override
    protected void acceptImpl(Webhook o, CommandResult result) {
        Webhook webhook = injectCommand(o);
        WebhookReceive receive = webhook.getReceive();
        ensureAlias(receive::getAlias, receive::setAlias);

        InboundDestination destination = coordinator.resolve(receive.getAlias(), dependencies.getEnvironment());

        logWebhookInfo(receive.getAlias(), destination.getEndpoint(), receive.getForward());
        addWebhookMetaData(receive.getAlias(), destination.getEndpoint(), receive.getForward(), result);

        ResponseProducer producer = buildResponseProducer(webhook, destination);

        Duration timeout = Duration.ofSeconds(webhook.getTimeout());

        // TODO: process correlation matcher
        CorrelationMatcher matcher = CorrelationMatcher.always();

        InboundExchange exchange = coordinator.awaitOneRequest(
                destination.getEndpoint(), currentTestId(), matcher, timeout, producer);

        compare(webhook, receive, exchange, result);
    }

    private void compare(Webhook webhook, WebhookReceive receive, InboundExchange exchange, CommandResult result) {
        if (exchange.getStatus() == InboundExchangeStatus.TIMEOUT) {
            throw new DefaultFrameworkException("No webhook received within " + webhook.getTimeout() + " seconds");
        }
        if (exchange.getStatus() == InboundExchangeStatus.ERROR) {
            throw new DefaultFrameworkException("Error processing webhook: " + exchange.getErrorMessage());
        }
        HttpValidator httpValidator = new HttpValidator(this, stringPrettifier);

        // Validation of the webhook payload
        validateReceiveBodyIfFile(receive, exchange, httpValidator, result);

        // Validation of the destination's response (skipped for mock mode)
        WebhookResponse response = webhook.getResponse();
        if (response != null && response.getExpected() != null && exchange.getResponseFromDestination() != null) {
            validateResponseFromDestination(response.getExpected(), exchange.getResponseFromDestination(), httpValidator, result);
        }
    }

    private void validateReceiveBodyIfFile(WebhookReceive receive, InboundExchange exchange, HttpValidator validator, CommandResult result) {
        String expectedBody = getContentIfFile(receive.getExpected());
        if (StringUtils.isNotBlank(expectedBody)) {
            String actualBody = new String(exchange.getReceivedRequest().getBody(), StandardCharsets.UTF_8);
            setContextBody(getContextBodyKey(receive.getExpected()), actualBody);
            result.setActual(stringPrettifier.asJsonResult(actualBody));
            result.setExpected(stringPrettifier.asJsonResult(expectedBody));
            validator.validateBody(expectedBody, actualBody);
        }
    }

    private void validateResponseFromDestination(WebhookResponseExpected expected, InboundResponse actual, HttpValidator validator, CommandResult result) {
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

    private ResponseProducer buildResponseProducer(Webhook webhook, InboundDestination destination) {
        WebhookReceive receive = webhook.getReceive();
        if (receive.getForward()) {
            // TODO: made forwarding to the destination
            return null;
        }
        InboundResponse mockResponse = buildMockResponse(webhook.getResponse());
        return request -> InboundExchangeResponseResult.mock(mockResponse);
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
}
