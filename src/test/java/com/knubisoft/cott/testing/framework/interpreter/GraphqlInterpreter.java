package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.http.ApiClient;
import com.knubisoft.cott.testing.framework.interpreter.lib.http.ApiResponse;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.HttpUtil;
import com.knubisoft.cott.testing.framework.util.HttpValidator;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.global_config.GraphqlApi;
import com.knubisoft.cott.testing.model.scenario.Graphql;
import com.knubisoft.cott.testing.model.scenario.GraphqlBody;
import com.knubisoft.cott.testing.model.scenario.GraphqlGet;
import com.knubisoft.cott.testing.model.scenario.GraphqlPost;
import com.knubisoft.cott.testing.model.scenario.Header;
import com.knubisoft.cott.testing.model.scenario.HttpInfo;
import com.knubisoft.cott.testing.model.scenario.Param;
import com.knubisoft.cott.testing.model.scenario.Response;
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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(Graphql.class)
public class GraphqlInterpreter extends AbstractInterpreter<Graphql> {

    @Autowired
    private ApiClient apiClient;

    public GraphqlInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @SneakyThrows
    @Override
    public void acceptImpl(final Graphql graphql, final CommandResult result) {
        GraphqlMetadata graphqlMetadata = Objects.nonNull(graphql.getPost())
                ? new GraphqlMetadata(graphql.getPost(), HttpMethod.POST)
                : new GraphqlMetadata(graphql.getGet(), HttpMethod.GET);
        ResultUtil.addGraphQlMetaData(graphql.getAlias(), graphqlMetadata, result);
        LogUtil.logGraphqlInfo(graphql.getAlias(), graphqlMetadata);
        ApiResponse response = getResponse(graphql, graphqlMetadata);
        compareResult(graphqlMetadata.getHttpInfo().getResponse(), response, result);
    }

    //CHECKSTYLE:OFF
    private ApiResponse getResponse(final Graphql graphql,
                                    final GraphqlMetadata graphqlMetadata) {
        HttpInfo httpInfo = graphqlMetadata.getHttpInfo();
        LogUtil.logGraphqlInfo(graphql.getAlias(), graphqlMetadata);
        Map<String, String> headers = getHeaders(httpInfo);
        String typeValue = headers.getOrDefault(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity body = getBody(httpInfo, ContentType.create(typeValue));
        LogUtil.logBodyContent(body);
        String fullUrl = httpInfo instanceof GraphqlGet
                ? urlWithParams(graphql, (GraphqlGet) httpInfo)
                : getFullUrl(graphql, httpInfo);
        try {
            return apiClient.call(graphqlMetadata.getHttpMethod(), fullUrl, headers, body);
        } catch (IOException e) {
            LogUtil.logError(e);
            throw new DefaultFrameworkException(e);
        }
    }
    //CHECKSTYLE:ON

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

    @SneakyThrows
    private String urlWithParams(final Graphql graphql, final GraphqlGet graphqlGet) {
        URIBuilder uriBuilder = new URIBuilder(getFullUrl(graphql, graphqlGet));
        List<Param> parameters = graphqlGet.getParam();
        parameters.forEach(param -> uriBuilder.addParameter(param.getName(), prettifyString(param.getData())));
        return uriBuilder.build().toString();
    }

    private Map<String, String> getHeaders(final HttpInfo httpInfo) {
        Map<String, String> headers = new LinkedHashMap<>();
        InterpreterDependencies.Authorization authorization = dependencies.getAuthorization();
        HttpUtil.fillHeadersMap(httpInfo.getHeader(), headers, authorization);
        return headers;
    }

    private String getFullUrl(final Graphql graphql, final HttpInfo httpInfo) {
        List<GraphqlApi> apiList = GlobalTestConfigurationProvider.getIntegrations().getGraphqlIntegration().getApi();
        GraphqlApi graphqlApi = (GraphqlApi) ConfigUtil.findApiForAlias(apiList, graphql.getAlias());
        return graphqlApi.getUrl() + httpInfo.getEndpoint();
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
        result.setActual(PrettifyStringJson.getJSONResult(actualBody));
        result.setExpected(PrettifyStringJson.getJSONResult(body));
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
