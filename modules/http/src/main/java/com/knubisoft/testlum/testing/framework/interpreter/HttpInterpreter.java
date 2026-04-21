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
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.scenario.Body;
import com.knubisoft.testlum.testing.model.scenario.Header;
import com.knubisoft.testlum.testing.model.scenario.Http;
import com.knubisoft.testlum.testing.model.scenario.HttpInfo;
import com.knubisoft.testlum.testing.model.scenario.HttpInfoWithBody;
import com.knubisoft.testlum.testing.model.scenario.Mode;
import com.knubisoft.testlum.testing.model.scenario.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(Http.class)
public class HttpInterpreter extends AbstractInterpreter<Http> {

    private static final String HTTP_METHOD_LOG = LogFormat.table("HTTP method");
    private static final String BODY_LOG = LogFormat.table("Body");
    private static final String HEADERS_LOG = LogFormat.table("Headers");


    private static final String SKIPPED_BODY_VALIDATION = "Validation of the response body was skipped "
            + "because of no expected file";

    private static final int MAX_CONTENT_LENGTH = 25 * 1024;

    private static final String API_ALIAS = "API alias";
    private static final String ENDPOINT = "Endpoint";
    private static final String HTTP_METHOD = "HTTP method";
    @Autowired
    private ApiClient apiClient;
    private final IntegrationsProvider integrationsProvider;
    private final HttpUtil httpUtil;

    public HttpInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.integrationsProvider = dependencies.getContext().getBean(IntegrationsProvider.class);
        this.httpUtil = dependencies.getContext().getBean(HttpUtil.class);
    }

    @Override
    protected void acceptImpl(final Http o, final CommandResult result) {
        Http http = injectCommand(o);
        ensureAlias(http::getAlias, http::setAlias);
        HttpUtil.HttpMethodMetadata metadata = httpUtil.getHttpMethodMetadata(http);
        HttpInfo httpInfo = metadata.getHttpInfo();
        HttpMethod httpMethod = metadata.getHttpMethod();
        ApiResponse actual = getActual(httpInfo, httpMethod, http.getAlias(), result);
        compareResult(httpInfo.getResponse(), actual, result);
    }

    private void compareResult(final Response expected,
                               final ApiResponse actual,
                               final CommandResult result) {
        String expectedFileName = expected.getFile();
        HttpValidator httpValidator = new HttpValidator(this, stringPrettifier, expectedFileName);
        String actualBody = actual.getBody();
        setContextBody(getContextBodyKey(expectedFileName), actualBody);
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
            result.setActual(stringPrettifier.asJsonResult(actualBody));
            result.setExpected(stringPrettifier.asJsonResult(body));
            httpValidator.validateBody(body, actualBody, Mode.STRICT.equals(expected.getMode()));
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
        logHeaders(headers);
        endpoint = this.httpUtil.sanitizeEndpointForAbsentKeywordsIfPresent(endpoint);
        logHttpInfo(alias, httpMethod.name(), endpoint);
        addHttpMetaData(alias, httpMethod.name(), headers, endpoint, result);
        ContentType contentType = this.httpUtil.computeContentType(headers);
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
            logException(e);
            throw new DefaultFrameworkException(e);
        }
    }

    private Map<String, String> getHeaders(final HttpInfo httpInfo) {
        Map<String, String> headers = new LinkedHashMap<>();
        InterpreterDependencies.Authorization authorization = dependencies.getAuthorization();
        httpUtil.fillHeadersMap(httpInfo.getHeader(), headers, authorization);
        List<Header> headersWithoutAbsentMarkedHeaders = this.httpUtil.sanitizeHeadersForAbsentKeyword(httpInfo);
        httpUtil.fillHeadersMap(headersWithoutAbsentMarkedHeaders, headers, authorization);
        return headers;
    }

    private HttpEntity getBody(final HttpInfo httpInfo, final ContentType contentType) {
        if (!(httpInfo instanceof HttpInfoWithBody commandWithBody)) {
            return null;
        }
        Body body = commandWithBody.getBody();
        return httpUtil.extractBody(body, contentType, this, dependencies);
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

    private void logBodyContent(final HttpEntity body) {
        if (Objects.nonNull(body) && body.getContentLength() < MAX_CONTENT_LENGTH) {
            try {
                String stringBody = IOUtils.toString(body.getContent(), StandardCharsets.UTF_8);
                if (StringUtils.isNotBlank(stringBody)) {
                    log.info(BODY_LOG, stringPrettifier.asJsonResult(stringPrettifier.cut(stringBody))
                            .replaceAll(LogFormat.newLine(), LogFormat.contentFormat()));
                }
            } catch (IOException e) {
                throw new DefaultFrameworkException(e);
            }
        }
    }

    @SneakyThrows
    private void logHeaders(final Map<String, String> headers) {
        if (!headers.isEmpty()) {
            log.info(HEADERS_LOG,
                    this.stringPrettifier.asJsonResult(headers)
                            .replaceAll(LogFormat.newLine(), LogFormat.contentFormat()));
        }
    }


    private void logBodyValidationSkipped() {
        log.info(SKIPPED_BODY_VALIDATION);
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

}
