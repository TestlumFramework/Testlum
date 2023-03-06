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
import com.knubisoft.cott.testing.framework.util.HttpValidator;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.global_config.GraphqlApi;
import com.knubisoft.cott.testing.model.scenario.Graphql;
import com.knubisoft.cott.testing.model.scenario.GraphqlBody;
import com.knubisoft.cott.testing.model.scenario.Header;
import com.knubisoft.cott.testing.model.scenario.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
        String query = getQuery(graphql.getBody());
        ResultUtil.addGraphQlMetaData(graphql.getAlias(), graphql.getEndpoint(), query, result);
        LogUtil.logGraphqlInfo(graphql.getAlias(), graphql.getEndpoint(), query);
        ApiResponse response = getResponse(graphql, query);
        compareResult(graphql.getResponse(), response, result);
    }

    private String getQuery(final GraphqlBody body) {
        String rawBody = StringUtils.isNotBlank(body.getRaw())
                ? body.getRaw()
                : FileSearcher.searchFileToString(body.getFrom().getFile(), dependencies.getFile());
        return inject(rawBody);
    }

    private ApiResponse getResponse(final Graphql graphql, final String query) throws IOException {
        String body = toString(new QueryBody(query));
        HttpPost post = buildHttpPost(graphql, body);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(post);
            return convertToApiResponse(response);
        }
    }

    private ApiResponse convertToApiResponse(final HttpResponse response) throws IOException {
        int code = response.getStatusLine().getStatusCode();
        Object body = EntityUtils.toString(response.getEntity());
        Map<String, String> headers = Arrays.stream(response.getAllHeaders())
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        return new ApiResponse(code, headers, body);
    }

    private HttpPost buildHttpPost(final Graphql graphql, final String body) {
        String url = getFullUrl(graphql);
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        return post;
    }

    private String getFullUrl(final Graphql graphql) {
        List<GraphqlApi> apiList = GlobalTestConfigurationProvider.getIntegrations().get(dependencies.getEnv())
                .getGraphqlIntegration().getApi();
        GraphqlApi graphqlApi = ConfigUtil.findApiForAlias(apiList, graphql.getAlias());
        return graphqlApi.getUrl() + graphql.getEndpoint();
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
        httpValidator.rethrowOnErrors();
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


    @AllArgsConstructor
    @Getter
    private static class QueryBody {
        private String query;
    }
}
