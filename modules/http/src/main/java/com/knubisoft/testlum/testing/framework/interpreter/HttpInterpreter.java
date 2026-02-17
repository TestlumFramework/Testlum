package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.ApiClient;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.ApiResponse;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.HttpValidator;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.util.HttpUtil;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.scenario.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(Http.class)
public class HttpInterpreter extends AbstractInterpreter<Http> {

    private static final String ALIAS_LOG = LogFormat.table("Alias");
    private static final String HTTP_METHOD_LOG = LogFormat.table("HTTP method");
    private static final String ENDPOINT_LOG = LogFormat.table("Endpoint");
    private static final String BODY_LOG = LogFormat.table("Body");

    private static final String CONTENT_FORMAT = String.format("%n%19s| %-23s|", StringUtils.EMPTY, StringUtils.EMPTY);
    private static final String SKIPPED_BODY_VALIDATION = "Validation of the response body was skipped "
            + "because of no expected file";
    private static final String ERROR_LOG = "Error ->";
    private static final int MAX_CONTENT_LENGTH = 25 * 1024;

    private static final String API_ALIAS = "API alias";
    private static final String ENDPOINT = "Endpoint";
    private static final String HTTP_METHOD = "HTTP method";
    private static final String ADDITIONAL_HEADERS = "Additional headers";
    private static final String HEADER_TEMPLATE = "%s: %s";
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    @Autowired
    private ApiClient apiClient;
    private final IntegrationsProvider integrationsProvider;

    public HttpInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.integrationsProvider = dependencies.getContext().getBean(IntegrationsProvider.class);
    }

    @Override
    protected void acceptImpl(final Http o, final CommandResult result) {
        Http http = injectCommand(o);
        checkAlias(http);
        HttpUtil.HttpMethodMetadata metadata = HttpUtil.getHttpMethodMetadata(http);
        HttpInfo httpInfo = metadata.getHttpInfo();
        HttpMethod httpMethod = metadata.getHttpMethod();
        ApiResponse actual = getActual(httpInfo, httpMethod, http.getAlias(), result);
        compareResult(httpInfo.getResponse(), actual, result);
    }

    private void checkAlias(final Http http) {
        if (http.getAlias() == null) {
            http.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private void compareResult(final Response expected,
                               final ApiResponse actual,
                               final CommandResult result) {
        HttpValidator httpValidator = new HttpValidator(this);
        String actualBody = actual.getBody();
        setContextBody(getContextBodyKey(expected.getFile()), actualBody);
        httpValidator.validateCode(expected.getCode(), actual.getCode());
        validateHeaders(expected, actual, httpValidator);
        validateBody(expected, actualBody, httpValidator, result);
        httpValidator.rethrowOnErrors();
    }

    private void validateBody(final Response expected,
                              final String actualBody,
                              final HttpValidator httpValidator,
                              final CommandResult result) {
        String body = getContentIfFile(expected.getFile());
        if (StringUtils.isNotBlank(body)) {
            result.setActual(StringPrettifier.asJsonResult(actualBody));
            result.setExpected(StringPrettifier.asJsonResult(body));
            httpValidator.validateBody(body, actualBody, expected.getMode().value());
        } else {
            logBodyValidationSkipped();
        }
    }

    private void validateHeaders(final Response expected,
                                 final ApiResponse actual,
                                 final HttpValidator httpValidator) {
        if (!expected.getHeader().isEmpty()) {
            httpValidator.validateHeaders(getExpectedHeaders(expected), actual.getHeaders());
        }
    }

    private Map<String, String> getExpectedHeaders(final Response expected) {
        return expected.getHeader().stream().collect(Collectors.toMap(Header::getName, Header::getData));
    }

    protected ApiResponse getActual(final HttpInfo httpInfo,
                                    final HttpMethod httpMethod,
                                    final String alias,
                                    final CommandResult result) {
        String endpoint = httpInfo.getEndpoint();
        Map<String, String> headers = getHeaders(httpInfo);
        logHttpInfo(alias, httpMethod.name(), endpoint);
        addHttpMetaData(alias, httpMethod.name(), headers, endpoint, result);
        ContentType contentType = HttpUtil.computeContentType(headers);
        HttpEntity body = getBody(httpInfo, contentType);
        mergeContentTypeFromBody(headers, body);
        logBodyContent(body);
        String url = createFullUrl(endpoint, alias);
        return getApiResponse(httpMethod, url, headers, body);
    }

    private void mergeContentTypeFromBody(final Map<String, String> headers, final HttpEntity body) {
        if (Objects.nonNull(body) && Objects.nonNull(body.getContentType())) {
            String contentType = body.getContentType().getValue();
            headers.merge(HttpHeaders.CONTENT_TYPE, contentType,
                    (k, v) -> contentType.equalsIgnoreCase(v) ? v : contentType);
        }
    }

    private ApiResponse getApiResponse(final HttpMethod httpMethod,
                                       final String url,
                                       final Map<String, String> headers,
                                       final HttpEntity body) {
        try {
            return apiClient.call(httpMethod, url, headers, body);
        } catch (Exception e) {
            logError(e);
            throw new DefaultFrameworkException(e);
        }
    }

    private Map<String, String> getHeaders(final HttpInfo httpInfo) {
        Map<String, String> headers = new LinkedHashMap<>();
        InterpreterDependencies.Authorization authorization = dependencies.getAuthorization();
        HttpUtil.fillHeadersMap(httpInfo.getHeader(), headers, authorization);
        return headers;
    }

    private HttpEntity getBody(final HttpInfo httpInfo, final ContentType contentType) {
        if (!(httpInfo instanceof HttpInfoWithBody)) {
            return null;
        }
        HttpInfoWithBody commandWithBody = (HttpInfoWithBody) httpInfo;
        Body body = commandWithBody.getBody();
        return HttpUtil.extractBody(body, contentType, this, dependencies);
    }

    private String createFullUrl(final String endpoint, final String alias) {
        List<Api> apiList = integrationsProvider.findListByEnv(Api.class, dependencies.getEnvironment());
        Api apiIntegration = integrationsProvider.findApiForAlias(apiList, alias);
        return apiIntegration.getUrl() + endpoint;
    }

    private void logHttpInfo(final String alias, final String method, final String endpoint) {
        log.info(ALIAS_LOG, alias);
        log.info(HTTP_METHOD_LOG, method);
        log.info(ENDPOINT_LOG, endpoint);
    }

    @SneakyThrows
    private void logBodyContent(final HttpEntity body) {
        if (Objects.nonNull(body) && body.getContentLength() < MAX_CONTENT_LENGTH) {
            String stringBody = IOUtils.toString(body.getContent(), StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(stringBody)) {
                log.info(BODY_LOG,
                        StringPrettifier.asJsonResult(StringPrettifier.cut(stringBody))
                                .replaceAll(LogFormat.newLine(), CONTENT_FORMAT));
            }
        }
    }

    private void logBodyValidationSkipped() {
        log.info(SKIPPED_BODY_VALIDATION);
    }

    private void logError(final Exception ex) {
        log.error(ERROR_LOG, ex);
    }

    private void addHttpMetaData(final String alias,
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

    private void addHeadersMetaData(final Map<String, String> headers, final CommandResult result) {
        result.put(ADDITIONAL_HEADERS, headers.entrySet().stream()
                .map(e -> String.format(HEADER_TEMPLATE, e.getKey(), e.getValue()))
                .toList());
    }
}
