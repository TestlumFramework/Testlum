package com.testlum.testing.framework.interpreter;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.testlum.testing.framework.inbound.server.server.InboundServer;
import com.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.interpreter.lib.http.HttpValidator;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.webhook.BufferedWebhook;
import com.testlum.testing.framework.webhook.WebhookBuffer;
import com.testlum.testing.framework.webhook.WebhookEndpoint;
import com.testlum.testing.framework.webhook.WebhookIntegrationRegistry;
import com.testlum.testing.framework.webhook.WebhookMatcherType;
import com.testlum.testing.framework.webhook.WebhookMatchers;
import com.testlum.testing.model.scenario.Webhook;
import com.testlum.testing.model.scenario.WebhookHeader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Waits for a webhook on the endpoint of a webhook integration, replies to its sender
 * with status> and validates the received body and headers.
 */
@Slf4j
@InterpreterForClass(Webhook.class)
public class WebhookInterpreter extends AbstractInterpreter<Webhook> {

    private static final String ENDPOINT = "Endpoint";
    private static final String MATCH_VALUE = "Match value";
    private static final String TIMEOUT = "Timeout";
    private static final String REPLY_STATUS = "Reply status";
    private static final String RECEIVED_HEADERS = "Received headers";
    private static final String MATCH_VALUE_LOG = LogFormat.table(MATCH_VALUE);
    private static final String TIMEOUT_LOG = LogFormat.table(TIMEOUT);
    private static final String REPLY_STATUS_LOG = LogFormat.table(REPLY_STATUS);

    private final WebhookIntegrationRegistry integrations;
    private final WebhookBuffer buffer;
    private final InboundServer inboundServer;

    public WebhookInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.integrations = dependencies.getContext().getBean(WebhookIntegrationRegistry.class);
        this.buffer = dependencies.getContext().getBean(WebhookBuffer.class);
        this.inboundServer = dependencies.getContext().getBean(InboundServer.class);
    }

    @Override
    protected void acceptImpl(final Webhook o, final CommandResult result) {
        Webhook webhook = injectCommand(o);
        String alias = webhook.getAlias();
        WebhookEndpoint endpoint = findEndpoint(alias);
        checkInboundServerIsRunning();
        Duration timeout = Duration.ofSeconds(webhook.getTimeout());
        int replyStatus = Objects.nonNull(webhook.getStatus())
                ? webhook.getStatus() : endpoint.getResponseStatusCode();
        logWebhookInfo(alias, endpoint, webhook);
        addWebhookMetaData(alias, endpoint, webhook, replyStatus, result);

        BufferedWebhook received = buffer
                .claim(endpoint, WebhookMatchers.forStep(alias, endpoint, webhook.getMatchValue()), timeout)
                .orElseThrow(() -> noWebhookReceived(alias, endpoint, webhook));
        int sentStatus = received.reply(replyStatus);
        checkReplyStatusWasSent(alias, webhook, received, sentStatus);
        validate(webhook, received.getRequest(), result);
    }

    private WebhookEndpoint findEndpoint(final String alias) {
        return integrations.find(alias, dependencies.getEnvironment())
                .orElseThrow(() -> new DefaultFrameworkException(String.format(
                        "Webhook integration with alias '%s' is not configured or not enabled for environment '%s'",
                                alias, dependencies.getEnvironment())));
    }

    private void checkInboundServerIsRunning() {
        if (!inboundServer.isRunning()) {
            throw new DefaultFrameworkException("<webhook> needs the inbound HTTP server. "
                    + "Enable it in the global config: <httpServer enabled=\"true\">");
        }
    }

    private void validate(final Webhook webhook, final InboundRequest request, final CommandResult result) {
        String actualBody = new String(request.getBody(), StandardCharsets.UTF_8);
        setContextBody(getContextBodyKey(webhook.getExpected()), actualBody);
        Map<String, String> actualHeaders = firstValues(request.getHeaders());
        result.put(RECEIVED_HEADERS, actualHeaders.entrySet().stream()
                .map(e -> String.format(HEADER_TEMPLATE, e.getKey(), e.getValue()))
                .toList());

        HttpValidator validator = new HttpValidator(this, stringPrettifier);
        validateHeaders(webhook, actualHeaders, validator);
        validateBody(webhook, actualBody, validator, result);
        validator.rethrowOnErrors();
    }

    private void validateHeaders(final Webhook webhook,
                                 final Map<String, String> actualHeaders,
                                 final HttpValidator validator) {
        if (Objects.isNull(webhook.getHeaders()) || webhook.getHeaders().getHeader().isEmpty()) {
            return;
        }
        Map<String, String> expectedHeaders = new LinkedHashMap<>();
        for (WebhookHeader header : webhook.getHeaders().getHeader()) {
            expectedHeaders.put(header.getName(), header.getValue());
        }
        validator.validateHeaders(expectedHeaders, actualHeaders);
    }

    private void validateBody(final Webhook webhook,
                              final String actualBody,
                              final HttpValidator validator,
                              final CommandResult result) {
        String expectedBody = getContentIfFile(webhook.getExpected());
        if (StringUtils.isNotBlank(expectedBody)) {
            result.setActual(stringPrettifier.asJsonResult(actualBody));
            result.setExpected(stringPrettifier.asJsonResult(expectedBody));
            validator.validateBody(expectedBody, actualBody);
        }
    }

    /**
     * The sender gets the step's status only if the webhook was still held when the step claimed it.
     * Otherwise, it already got the integration's responseStatusCode, and the scenario no longer
     * tests what it intends to.
     */
    private void checkReplyStatusWasSent(final String alias,
                                         final Webhook webhook,
                                         final BufferedWebhook received,
                                         final int sentStatus) {
        if (Objects.nonNull(webhook.getStatus()) && webhook.getStatus() != sentStatus) {
            throw new DefaultFrameworkException(String.format(
                    "Webhook on alias '%s' arrived %ds before this step claimed it and was already answered with "
                            + "%d after the integration's holdTimeout (%ds), so <status>%d</status> couldn't be sent. "
                            + "Increase holdTimeout of the webhook integration.",
                    alias, received.getAge().toSeconds(), sentStatus,
                    received.getEndpoint().getHoldTimeout().toSeconds(), webhook.getStatus()));
        }
        log.info(REPLY_STATUS_LOG, sentStatus);
    }

    private DefaultFrameworkException noWebhookReceived(final String alias,
                                                        final WebhookEndpoint endpoint,
                                                        final Webhook webhook) {
        StringBuilder message = new StringBuilder(String.format(
                "No webhook received on alias '%s' (%s) within %d seconds", alias, endpoint, webhook.getTimeout()));
        if (endpoint.getMatcherType() != WebhookMatcherType.NONE) {
            message.append(String.format(" with %s '%s' = '%s'",
                    WebhookMatchers.describe(endpoint.getMatcherType()), endpoint.getMatcherKey(),
                    webhook.getMatchValue()));
            int unmatched = buffer.size(endpoint);
            if (unmatched > 0) {
                message.append(String.format(". %d other webhook(s) on this endpoint didn't match", unmatched));
            }
        }
        return new DefaultFrameworkException(message.toString());
    }

    private Map<String, String> firstValues(final Map<String, List<String>> headers) {
        Map<String, String> flat = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.forEach((name, values) -> {
            if (Objects.nonNull(values) && !values.isEmpty()) {
                flat.put(name, values.get(0));
            }
        });
        return flat;
    }

    private void logWebhookInfo(final String alias,
                                final WebhookEndpoint endpoint,
                                final Webhook webhook) {
        log.info(ALIAS_LOG, alias);
        log.info(ENDPOINT_LOG, endpoint);
        if (StringUtils.isNotBlank(webhook.getMatchValue())) {
            log.info(MATCH_VALUE_LOG, webhook.getMatchValue());
        }
        log.info(TIMEOUT_LOG, webhook.getTimeout() + "s");
    }

    private void addWebhookMetaData(final String alias,
                                    final WebhookEndpoint endpoint,
                                    final Webhook webhook,
                                    final int replyStatus,
                                    final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ENDPOINT, endpoint.getPath());
        if (StringUtils.isNotBlank(webhook.getMatchValue())) {
            result.put(MATCH_VALUE, webhook.getMatchValue());
        }
        result.put(TIMEOUT, webhook.getTimeout() + "s");
        result.put(REPLY_STATUS, replyStatus);
    }
}
