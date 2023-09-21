package com.knubisoft.testlum.testing.framework.interpreter.lib.http.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;

@UtilityClass
public class ResultUtil {

    private static final String ALIAS = "Alias";
    private static final String API_ALIAS = "API alias";
    private static final String ENDPOINT = "Endpoint";
    private static final String HTTP_METHOD = "HTTP method";
    private static final String ADDITIONAL_HEADERS = "Additional headers";
    private static final String HEADER_TEMPLATE = "%s: %s";
    private static final String LAMBDA_FUNCTION_NAME = "Function name";
    private static final String LAMBDA_PAYLOAD = "Payload";

    public void addGraphQlMetaData(final String alias,
                                   final HttpMethod httpMethod,
                                   final Map<String, String> headers,
                                   final String endpoint,
                                   final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(HTTP_METHOD, httpMethod);
        result.put(ENDPOINT, endpoint);
        if (!headers.isEmpty()) {
            addHeadersMetaData(headers, result);
        }
    }

    public void addSendGridMetaData(final String alias,
                                    final String httpMethodName,
                                    final Map<String, String> headers,
                                    final String endpoint,
                                    final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ENDPOINT, endpoint);
        result.put(HTTP_METHOD, httpMethodName);
        if (!headers.isEmpty()) {
            addHeadersMetaData(headers, result);
        }
    }

    public void addElasticsearchMetaData(final String alias,
                                         final String httpMethodName,
                                         final Map<String, String> headers,
                                         final String endpoint,
                                         final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ENDPOINT, endpoint);
        result.put(HTTP_METHOD, httpMethodName);
        if (!headers.isEmpty()) {
            addHeadersMetaData(headers, result);
        }
    }

    public void addLambdaGeneralMetaData(final String alias,
                                         final String functionName,
                                         final String payload,
                                         final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(LAMBDA_FUNCTION_NAME, functionName);
        result.put(LAMBDA_PAYLOAD, StringPrettifier.asJsonResult(payload));
    }

    private void addHeadersMetaData(final Map<String, String> headers, final CommandResult result) {
        result.put(ADDITIONAL_HEADERS, headers.entrySet().stream()
                .map(e -> format(HEADER_TEMPLATE, e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
    }

    public void addHttpMetaData(final String alias,
                                final String httpMethodName,
                                final Map<String, String> headers,
                                final String endpoint,
                                final CommandResult result) {
        result.put(API_ALIAS, alias);
        result.put(ENDPOINT, endpoint);
        result.put(HTTP_METHOD, httpMethodName);
        if (!headers.isEmpty()) {
            addHeadersMetaData(headers, result);
        }
    }
}
