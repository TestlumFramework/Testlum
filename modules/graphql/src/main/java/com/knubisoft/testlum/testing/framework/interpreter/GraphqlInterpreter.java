package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
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
import com.knubisoft.testlum.testing.model.global_config.GraphqlApi;
import com.knubisoft.testlum.testing.model.scenario.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
@InterpreterForClass(Graphql.class)
public class GraphqlInterpreter extends AbstractInterpreter<Graphql> {

    //LOGS
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String HTTP_METHOD_LOG = format(TABLE_FORMAT, "HTTP method", "{}");
    private static final String ENDPOINT_LOG = format(TABLE_FORMAT, "Endpoint", "{}");
    private static final String BODY_LOG = format(TABLE_FORMAT, "Body", "{}");
    private static final String REGEX_NEW_LINE = "[\\r\\n]";
    private static final String CONTENT_FORMAT = format("%n%19s| %-23s|", EMPTY, EMPTY);
    private static final String INCORRECT_HTTP_PROCESSING = "Incorrect http processing";
    private static final String ERROR_LOG = "Error ->";
    private static final int MAX_CONTENT_LENGTH = 25 * 1024;

    //RESULT
    private static final String ALIAS = "Alias";
    private static final String ENDPOINT = "Endpoint";
    private static final String HTTP_METHOD = "HTTP method";
    private static final String ADDITIONAL_HEADERS = "Additional headers";
    private static final String HEADER_TEMPLATE = "%s: %s";
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    private final Map<Function<Graphql, HttpInfo>, HttpMethod> graphqlMethodMap;

    @Autowired
    private ApiClient apiClient;
    private final IntegrationsProvider integrationsProvider;

    public GraphqlInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.integrationsProvider = dependencies.getContext().getBean(IntegrationsProvider.class);
        graphqlMethodMap = Map.of(Graphql::getGet, HttpMethod.GET, Graphql::getPost, HttpMethod.POST);
    }

    @Override
    public void acceptImpl(final Graphql o, final CommandResult result) {
        Graphql graphql = injectCommand(o);
        checkAlias(graphql);
        GraphqlMetadata graphqlMetadata = getGraphqlMetaData(graphql);
        HttpInfo httpInfo = graphqlMetadata.httpInfo;
        HttpMethod httpMethod = graphqlMetadata.getHttpMethod();
        ApiResponse response = getActualResponse(httpInfo, httpMethod, graphql.getAlias(), result);
        compareResult(graphqlMetadata.getHttpInfo().getResponse(), response, result);
    }

    private void checkAlias(final Graphql graphql) {
        if (graphql.getAlias() == null) {
            graphql.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private GraphqlMetadata getGraphqlMetaData(final Graphql graphql) {
        return graphqlMethodMap.entrySet().stream()
                .map(e -> new GraphqlMetadata(e.getKey().apply(graphql), e.getValue()))
                .filter(p -> Objects.nonNull(p.getHttpInfo()))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(INCORRECT_HTTP_PROCESSING));
    }

    private ApiResponse getActualResponse(final HttpInfo httpInfo,
                                          final HttpMethod httpMethod,
                                          final String alias,
                                          final CommandResult result) {
        String endpoint = httpInfo.getEndpoint();
        Map<String, String> headers = getHeaders(httpInfo);
        logHttpInfo(alias, httpMethod.name(), endpoint);
        addGraphQlMetaData(alias, httpMethod, headers, endpoint, result);
        ContentType contentType = HttpUtil.computeContentType(headers);
        HttpEntity body = getBody(httpInfo, contentType);
        logBodyContent(body);
        String url = getFullUrl(httpInfo, endpoint, alias);
        return getApiResponse(httpMethod, url, headers, body);
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
        if (!(httpInfo instanceof GraphqlPost)) {
            return null;
        }
        GraphqlPost graphqlPost = (GraphqlPost) httpInfo;
        String rawBody = getRawBody(graphqlPost.getBody());
        return new StringEntity(rawBody, contentType);
    }

    private String getRawBody(final GraphqlBody body) {
        String rawBody = StringUtils.isNotBlank(body.getRaw())
                ? body.getRaw()
                : getContentIfFile(body.getFrom().getFile());
        return prettifyString(rawBody);
    }

    private String getFullUrl(final HttpInfo httpInfo, final String endpoint, final String alias) {
        List<GraphqlApi> apiList = integrationsProvider.findListByEnv(GraphqlApi.class, dependencies.getEnvironment());
        GraphqlApi graphqlApi = integrationsProvider.findApiForAlias(apiList, alias);
        String url = graphqlApi.getUrl() + endpoint;
        if (httpInfo instanceof GraphqlGet) {
            return urlWithParams((GraphqlGet) httpInfo, url);
        }
        return url;
    }

    @SneakyThrows
    private String urlWithParams(final GraphqlGet graphqlGet, final String url) {
        URIBuilder uriBuilder = new URIBuilder(url);
        List<Param> parameters = graphqlGet.getParam();
        parameters.forEach(param -> uriBuilder.addParameter(param.getName(), prettifyString(param.getData())));
        return uriBuilder.build().toString();
    }

    private void compareResult(final Response expected,
                               final ApiResponse actual,
                               final CommandResult result) {
        HttpValidator httpValidator = new HttpValidator(this);
        httpValidator.validateCode(expected.getCode(), actual.getCode());
        String actualBody = String.valueOf(actual.getBody());
        setContextBody(getContextBodyKey(expected.getFile()), actualBody);
        validateBody(expected, actualBody, httpValidator, result);
        validateHeaders(expected, actual, httpValidator);
        httpValidator.rethrowOnErrors();
    }

    private void validateBody(final Response expected,
                              final String actualBody,
                              final HttpValidator httpValidator,
                              final CommandResult result) {
        String body = StringUtils.isBlank(expected.getFile())
                ? DelimiterConstant.EMPTY
                : getContentIfFile(expected.getFile());
        result.setActual(StringPrettifier.asJsonResult(actualBody));
        result.setExpected(StringPrettifier.asJsonResult(body));
        httpValidator.validateBody(body, actualBody);
    }

    private void validateHeaders(final Response expected,
                                 final ApiResponse actual,
                                 final HttpValidator httpValidator) {
        if (!expected.getHeader().isEmpty()) {
            httpValidator.validateHeaders(getExpectedHeaders(expected), actual.getHeaders());
        }
    }

    private Map<String, String> getExpectedHeaders(final Response expected) {
        return expected.getHeader().stream()
                .collect(Collectors.toMap(Header::getName, Header::getData));
    }

    public String prettifyString(final String string) {
        return string.replaceAll(DelimiterConstant.REGEX_MANY_SPACES, DelimiterConstant.SPACE);
    }

    //LOGS
    private void logHttpInfo(final String alias, final String method, final String endpoint) {
        log.info(ALIAS_LOG, alias);
        log.info(HTTP_METHOD_LOG, method);
        log.info(ENDPOINT_LOG, endpoint);
    }

    @SneakyThrows
    private void logBodyContent(final HttpEntity body) {
        if (nonNull(body) && body.getContentLength() < MAX_CONTENT_LENGTH) {
            String stringBody = IOUtils.toString(body.getContent(), StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(stringBody)) {
                log.info(BODY_LOG,
                        StringPrettifier.asJsonResult(StringPrettifier.cut(stringBody))
                                .replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
            }
        }
    }

    private void logError(final Exception ex) {
        log.error(ERROR_LOG, ex);
    }

    //RESULT
    private void addGraphQlMetaData(final String alias,
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

    private void addHeadersMetaData(final Map<String, String> headers, final CommandResult result) {
        result.put(ADDITIONAL_HEADERS, headers.entrySet().stream()
                .map(e -> format(HEADER_TEMPLATE, e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
    }

    @RequiredArgsConstructor
    @Getter
    public static class GraphqlMetadata {
        private final HttpInfo httpInfo;
        private final HttpMethod httpMethod;
    }
}
