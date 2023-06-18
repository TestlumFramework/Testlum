package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.HttpUtil;
import com.knubisoft.testlum.testing.framework.util.HttpValidator;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Body;
import com.knubisoft.testlum.testing.model.scenario.ElasticSearchRequest;
import com.knubisoft.testlum.testing.model.scenario.ElasticSearchRequestWithBody;
import com.knubisoft.testlum.testing.model.scenario.ElasticSearchResponse;
import com.knubisoft.testlum.testing.model.scenario.Elasticsearch;
import com.knubisoft.testlum.testing.model.scenario.Header;
import com.knubisoft.testlum.testing.model.scenario.Param;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;


@Slf4j
@InterpreterForClass(Elasticsearch.class)
public class ElasticsearchInterpreter extends AbstractInterpreter<Elasticsearch> {

    @Autowired(required = false)
    private Map<AliasEnv, RestClient> restClient;

    public ElasticsearchInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Elasticsearch o, final CommandResult result) {
        Elasticsearch elasticsearch = injectCommand(o);
        HttpUtil.ESHttpMethodMetadata esHttpMethodMetadata = HttpUtil.getESHttpMethodMetadata(elasticsearch);
        ElasticSearchRequest elasticSearchRequest = esHttpMethodMetadata.getElasticSearchRequest();
        HttpMethod httpMethod = esHttpMethodMetadata.getHttpMethod();
        Response actual = getActual(elasticSearchRequest, httpMethod, elasticsearch.getAlias(), result);
        ElasticSearchResponse expected = elasticSearchRequest.getResponse();
        compare(expected, actual, result);
    }

    private void compare(final ElasticSearchResponse expected, final Response actual, final CommandResult result) {
        HttpValidator httpValidator = new HttpValidator(this);
        httpValidator.validateCode(expected.getCode(), actual.getStatusLine().getStatusCode());
        validateHeadersIfExists(expected, actual, httpValidator);
        validateBodyIfFile(expected, actual, httpValidator, result);
        httpValidator.rethrowOnErrors();
    }

    @SneakyThrows
    private void validateBodyIfFile(final ElasticSearchResponse expectedResponse,
                                    final Response actual,
                                    final HttpValidator httpValidator,
                                    final CommandResult result) {
        String expectedBody = getContentIfFile(expectedResponse.getFile());
        if (StringUtils.isNotBlank(expectedBody)) {
            String actualBody = nonNull(actual.getEntity()) ? EntityUtils.toString(actual.getEntity()) : null;
            setContextBody(actualBody);
            result.setActual(StringPrettifier.asJsonResult(actualBody));
            result.setExpected(StringPrettifier.asJsonResult(expectedBody));
            httpValidator.validateBody(expectedBody, actualBody);
        }
    }

    private void validateHeadersIfExists(final ElasticSearchResponse expected,
                                         final Response actual,
                                         final HttpValidator httpValidator) {
        if (!expected.getHeader().isEmpty()) {
            Map<String, String> actualHeaderMap = Arrays.stream(actual.getHeaders())
                    .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

            Map<String, String> expectedHeaderMap = expected.getHeader().stream()
                    .collect(Collectors.toMap(Header::getName, Header::getData));

            httpValidator.validateHeaders(expectedHeaderMap, actualHeaderMap);
        }
    }

    @SneakyThrows
    private Response getActual(final ElasticSearchRequest elasticSearchRequest,
                               final HttpMethod httpMethod,
                               final String alias,
                               final CommandResult result) {
        String endpoint = elasticSearchRequest.getEndpoint();
        Map<String, String> headers = getHeaders(elasticSearchRequest);
        LogUtil.logHttpInfo(alias, httpMethod.name(), endpoint);
        ResultUtil.addElasticsearchMetaData(alias, httpMethod.name(), headers, endpoint, result);
        Request request = buildRequest(elasticSearchRequest, httpMethod, endpoint, headers);
        try {
            return restClient.get(new AliasEnv(alias, dependencies.getEnvironment())).performRequest(request);
        } catch (ResponseException responseException) {
            LogUtil.logError(responseException);
            return responseException.getResponse();
        }
    }

    private Request buildRequest(final ElasticSearchRequest elasticSearchRequest,
                                 final HttpMethod httpMethod,
                                 final String endpoint,
                                 final Map<String, String> headers) {
        Request request = new Request(httpMethod.name(), endpoint);
        setRequestOptions(headers, request);

        Map<String, String> params = getParams(elasticSearchRequest);
        request.addParameters(params);

        ContentType contentType = HttpUtil.computeContentType(headers);
        HttpEntity body = getBody(elasticSearchRequest, contentType);
        LogUtil.logBodyContent(body);
        request.setEntity(body);
        return request;
    }

    private void setRequestOptions(final Map<String, String> headers, final Request request) {
        RequestOptions.Builder requestOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
        for (Map.Entry<String, String> entryHeaderMap : headers.entrySet()) {
            requestOptionsBuilder.addHeader(entryHeaderMap.getKey(), entryHeaderMap.getValue());
        }
        request.setOptions(requestOptionsBuilder);
    }

    private Map<String, String> getHeaders(final ElasticSearchRequest elasticSearchRequest) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (Header header : elasticSearchRequest.getHeader()) {
            headers.put(header.getName(), header.getData());
        }
        return headers;
    }

    private Map<String, String> getParams(final ElasticSearchRequest request) {
        return request.getParam().stream()
                .collect(Collectors.toMap(Param::getName, Param::getData));
    }

    private HttpEntity getBody(final ElasticSearchRequest request, final ContentType contentType) {
        if (!(request instanceof ElasticSearchRequestWithBody)) {
            return null;
        }
        ElasticSearchRequestWithBody requestWithBody = (ElasticSearchRequestWithBody) request;
        Body body = requestWithBody.getBody();
        return HttpUtil.extractBody(body, contentType, this, dependencies);
    }
}
