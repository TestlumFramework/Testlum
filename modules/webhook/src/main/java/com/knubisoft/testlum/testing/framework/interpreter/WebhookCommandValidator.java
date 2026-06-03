package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.HttpValidator;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.retry.RetryAttempt;
import com.knubisoft.testlum.testing.framework.util.ContentResolver;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.WebhookExpected;
import com.knubisoft.testlum.testing.model.scenario.WebhookHeader;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WebhookCommandValidator {

    private final ContentResolver contentResolver;
    private final StringPrettifier stringPrettifier;
    private final InterpreterDependencies dependencies;

    public WebhookCommandValidator(final ContentResolver contentResolver,
                                   final StringPrettifier stringPrettifier,
                                   final InterpreterDependencies dependencies) {
        this.contentResolver = contentResolver;
        this.stringPrettifier = stringPrettifier;
        this.dependencies = dependencies;
    }

    public ValidationAction receiveBodyValidator(final RetryAttempt attempt,
                                                 final InboundRequest request) {
        return (validator, subResult)
                -> validateReceivedBody(attempt.getExpectedRequestFile(), request, validator, subResult);
    }

    public ValidationAction responseFromDestinationValidator(final WebhookExpected expected,
                                                             final InboundResponse actual) {
        return (validator, subResult)
                -> validateResponseFromDestination(expected, actual, validator, subResult);
    }

    private void validateReceivedBody(final String expectedFile,
                                      final InboundRequest request,
                                      final HttpValidator validator,
                                      final CommandResult result) {
        String expectedBody = contentResolver.getContentIfFile(expectedFile);
        if (StringUtils.isNotBlank(expectedBody)) {
            String actualBody = new String(request.getBody(), StandardCharsets.UTF_8);
            dependencies.getScenarioContext().set(getContextBodyKey(expectedFile), actualBody);
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

    private void validateResponseFromDestination(final WebhookExpected expected,
                                                 final InboundResponse actual,
                                                 final HttpValidator validator,
                                                 final CommandResult result) {
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

        String expectedBody = contentResolver.getContentIfFile(expected.getFile());
        if (StringUtils.isNotBlank(expectedBody)) {
            String actualBody = new String(actual.getBody(), StandardCharsets.UTF_8);
            dependencies.getScenarioContext().set(getContextBodyKey(expected.getFile()), actualBody);
            result.setActual(stringPrettifier.asJsonResult(actualBody));
            result.setExpected(stringPrettifier.asJsonResult(expectedBody));
            validator.validateBody(expectedBody, actualBody);
        }
    }

    private Map<String, String> firstValueHeader(final Map<String, List<String>> headers) {
        Map<String, String> flatHeaders = new LinkedHashMap<>();
        headers.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                flatHeaders.put(key, value.get(0));
            }
        });
        return flatHeaders;
    }

    public String getContextBodyKey(final String fileOrContent) {
        if (StringUtils.isNotBlank(fileOrContent)) {
            return fileOrContent;
        }
        return UUID.randomUUID().toString();
    }

}
