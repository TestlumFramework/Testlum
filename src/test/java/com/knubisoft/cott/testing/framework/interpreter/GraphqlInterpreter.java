package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
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
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(Graphql.class)
public class GraphqlInterpreter extends AbstractInterpreter<Graphql> {

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

    private ApiResponse getResponse(final Graphql graphql,
                                    final GraphqlMetadata graphqlMetadata) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpUriRequest httpRequest = graphqlMetadata.getHttpMethod() == HttpMethod.POST
                    ? buildHttpPost(graphql, (GraphqlPost) graphqlMetadata.getHttpInfo())
                    : buildHttpGet(graphql, (GraphqlGet) graphqlMetadata.getHttpInfo());
            setHeaders(httpRequest, graphqlMetadata.getHttpInfo());
            HttpResponse response = client.execute(httpRequest);
            return convertToApiResponse(response);
        }
    }

    private HttpUriRequest buildHttpPost(final Graphql graphql, final GraphqlPost graphqlPost) {
        HttpPost post = new HttpPost(getFullUrl(graphql, graphqlPost));
        String rawBody = getRawBody(graphqlPost.getBody());
        LogUtil.logBody(rawBody);
        post.setEntity(new StringEntity(rawBody, ContentType.APPLICATION_JSON));
        return post;
    }


    private String getRawBody(final GraphqlBody body) {
        String rawBody = StringUtils.isNotBlank(body.getRaw())
                ? body.getRaw()
                : FileSearcher.searchFileToString(body.getFrom().getFile(), dependencies.getFile());
        return prettifyString(inject(rawBody));
    }

    @SneakyThrows
    private HttpGet buildHttpGet(final Graphql graphql, final GraphqlGet graphqlGet) {
        URIBuilder uriBuilder = new URIBuilder(getFullUrl(graphql, graphqlGet));
        List<Param> parameters = graphqlGet.getParameter();
        parameters.forEach(param -> uriBuilder.addParameter(param.getName(), prettifyString(param.getData())));
        return new HttpGet(uriBuilder.build());
    }

    private void setHeaders(final HttpUriRequest httpRequest, final HttpInfo httpInfo) {
        Map<String, String> headers = new LinkedHashMap<>();
        InterpreterDependencies.Authorization authorization = dependencies.getAuthorization();
        HttpUtil.fillHeadersMap(httpInfo.getHeader(), headers, authorization);
        headers.forEach(httpRequest::addHeader);
    }

    private String getFullUrl(final Graphql graphql, final HttpInfo httpInfo) {
        List<GraphqlApi> apiList = GlobalTestConfigurationProvider.getIntegrations().getGraphqlIntegration().getApi();
        GraphqlApi graphqlApi = (GraphqlApi) ConfigUtil.findApiForAlias(apiList, graphql.getAlias());
        return graphqlApi.getUrl() + httpInfo.getEndpoint();
    }

    private ApiResponse convertToApiResponse(final HttpResponse response) throws IOException {
        int code = response.getStatusLine().getStatusCode();
        Object body = EntityUtils.toString(response.getEntity());
        Map<String, String> headers = Arrays.stream(response.getAllHeaders())
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        return new ApiResponse(code, headers, body);
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
