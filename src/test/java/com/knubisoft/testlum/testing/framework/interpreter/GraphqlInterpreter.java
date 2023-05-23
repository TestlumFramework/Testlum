package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.ApiClient;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.ApiResponse;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.HttpUtil;
import com.knubisoft.testlum.testing.framework.util.HttpValidator;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.global_config.GraphqlApi;
import com.knubisoft.testlum.testing.model.scenario.Graphql;
import com.knubisoft.testlum.testing.model.scenario.GraphqlBody;
import com.knubisoft.testlum.testing.model.scenario.GraphqlGet;
import com.knubisoft.testlum.testing.model.scenario.GraphqlPost;
import com.knubisoft.testlum.testing.model.scenario.Header;
import com.knubisoft.testlum.testing.model.scenario.HttpInfo;
import com.knubisoft.testlum.testing.model.scenario.Param;
import com.knubisoft.testlum.testing.model.scenario.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INCORRECT_HTTP_PROCESSING;

@Slf4j
@InterpreterForClass(Graphql.class)
public class GraphqlInterpreter extends AbstractInterpreter<Graphql> {

    private final Map<Function<Graphql, HttpInfo>, HttpMethod> graphqlMethodMap;

    @Autowired
    private ApiClient apiClient;

    public GraphqlInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        Map<Function<Graphql, HttpInfo>, HttpMethod> methodMap = new HashMap<>();
        methodMap.put(Graphql::getGet, HttpMethod.GET);
        methodMap.put(Graphql::getPost, HttpMethod.POST);
        graphqlMethodMap = Collections.unmodifiableMap(methodMap);
    }

    @Override
    public void acceptImpl(final Graphql graphql, final CommandResult result) {
        GraphqlMetadata graphqlMetadata = getGraphqlMetaData(graphql);
        HttpInfo httpInfo = graphqlMetadata.httpInfo;
        HttpMethod httpMethod = graphqlMetadata.getHttpMethod();
        ApiResponse response = getActualResponse(httpInfo, httpMethod, graphql.getAlias(), result);
        compareResult(graphqlMetadata.getHttpInfo().getResponse(), response, result);
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
        String endpoint = inject(httpInfo.getEndpoint());
        Map<String, String> headers = getHeaders(httpInfo);
        LogUtil.logHttpInfo(alias, httpMethod.name(), endpoint);
        ResultUtil.addGraphQlMetaData(alias, httpMethod, headers, endpoint, result);
        String typeValue = headers.getOrDefault(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity body = getBody(httpInfo, ContentType.create(typeValue));
        LogUtil.logBodyContent(body);
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
            LogUtil.logError(e);
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
                : FileSearcher.searchFileToString(body.getFrom().getFile(), dependencies.getFile());
        return prettifyString(inject(rawBody));
    }

    private String getFullUrl(final HttpInfo httpInfo, final String endpoint, final String alias) {
        List<GraphqlApi> apiList = GlobalTestConfigurationProvider.getIntegrations().get(dependencies.getEnvironment())
                .getGraphqlIntegration().getApi();
        GraphqlApi graphqlApi = IntegrationsUtil.findApiForAlias(apiList, alias);
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
        setContextBody(actualBody);
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
                : FileSearcher.searchFileToString(expected.getFile(), dependencies.getFile());
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
        Map<String, String> expectedHeaders
                = expected.getHeader().stream().collect(Collectors.toMap(Header::getName, Header::getData));
        return HttpUtil.injectAndGetHeaders(expectedHeaders, this);
    }

    public String prettifyString(final String str) {
        return str.replaceAll(DelimiterConstant.REGEX_MANY_SPACES, DelimiterConstant.SPACE);
    }

    @RequiredArgsConstructor
    @Getter
    public static class GraphqlMetadata {
        private final HttpInfo httpInfo;
        private final HttpMethod httpMethod;
    }
}
