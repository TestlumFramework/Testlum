package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.util.LogUtil;
import com.knubisoft.e2e.testing.framework.util.PrettifyStringJson;
import com.knubisoft.e2e.testing.framework.util.ResultUtil;
import com.knubisoft.e2e.testing.model.scenario.ElasticSearchRequestWithBody;
import com.knubisoft.e2e.testing.model.scenario.Elasticsearch;
import com.knubisoft.e2e.testing.model.scenario.Header;
import com.knubisoft.e2e.testing.model.scenario.Param;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.HttpUtil;
import com.knubisoft.e2e.testing.framework.util.HttpValidator;
import com.knubisoft.e2e.testing.model.scenario.Body;
import com.knubisoft.e2e.testing.model.scenario.ElasticSearchRequest;
import com.knubisoft.e2e.testing.model.scenario.ElasticSearchResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
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


@Slf4j
@InterpreterForClass(Elasticsearch.class)
public class ElasticsearchInterpreter extends AbstractInterpreter<Elasticsearch> {

    @Autowired(required = false)
    private Map<String, RestClient> restClient;


    public ElasticsearchInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Elasticsearch elasticsearch, final CommandResult result) {
        HttpUtil.ESHttpMethodMetadata esHttpMethodMetadata = HttpUtil.getESHttpMethodMetadata(elasticsearch);
        ElasticSearchRequest elasticSearchRequest = esHttpMethodMetadata.getElasticSearchRequest();
        HttpMethod httpMethod = esHttpMethodMetadata.getHttpMethod();
        ResultUtil.addElasticsearchMetaData(elasticsearch.getAlias(), elasticSearchRequest, httpMethod.name(), result);
        Response actual = getActual(elasticSearchRequest, httpMethod, elasticsearch.getAlias());
        ElasticSearchResponse expected = elasticSearchRequest.getResponse();
        compare(expected, actual, result);
    }

    @SneakyThrows
    protected Response getActual(final ElasticSearchRequest elasticSearchRequest,
                               final HttpMethod httpMethod,
                               final String alias) {
        LogUtil.logHttpInfo(alias, httpMethod.name(), elasticSearchRequest.getUrl());
        Request request = buildRequest(elasticSearchRequest, httpMethod);
        try {
            return restClient.get(alias).performRequest(request);
        } catch (ResponseException responseException) {
            log.error("Failed response", responseException);
            return responseException.getResponse();
        }
    }

    protected ElasticSearchResponse getExpected(final ElasticSearchRequest elasticSearchRequest) {
        return elasticSearchRequest.getResponse();
    }

    protected void compare(final ElasticSearchResponse expected, final Response actual, final CommandResult result) {
        HttpValidator httpValidator = new HttpValidator(this);
        httpValidator.validateCode(expected.getCode(), actual.getStatusLine().getStatusCode());
        validateIfEmptyHeader(expected, actual, httpValidator);
        validateBodyIfFile(expected, actual, httpValidator, result);
        httpValidator.rethrowOnErrors();
    }

    @SneakyThrows
    private void validateBodyIfFile(final ElasticSearchResponse expectedResponse,
                                    final Response actual,
                                    final HttpValidator httpValidator,
                                    final CommandResult result) {
        String expectedFile = expectedResponse.getFile();
        if (expectedFile != null) {
            String actualBody = EntityUtils.toString(actual.getEntity());
            setContextBody(actualBody);
            String expectedBody = dependencies.getFileSearcher().searchFileToString(expectedFile);
            result.setActual(PrettifyStringJson.getJSONResult(actualBody));
            result.setExpected(PrettifyStringJson.getJSONResult(expectedBody));
            httpValidator.validateBody(expectedBody, actualBody);
        }
    }

    private void validateIfEmptyHeader(final ElasticSearchResponse expected,
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

    private Request buildRequest(final ElasticSearchRequest elasticSearchRequest,
                                 final HttpMethod httpMethod) {
        Map<String, String> headers = getHeaders(elasticSearchRequest);

        Request request = new Request(httpMethod.name(), elasticSearchRequest.getUrl());

        Map<String, String> params = getParams(elasticSearchRequest);
        request.addParameters(params);

        setRequestOptions(headers, request);

        HttpEntity body = getBody(elasticSearchRequest);
        LogUtil.logBodyContent(body);
        request.setEntity(body);
        return request;
    }

    private void setRequestOptions(final Map<String, String> headers,
                                   final Request request) {
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

        return HttpUtil.injectAndGetHeaders(headers, this);
    }

    private Map<String, String> getParams(final ElasticSearchRequest request) {
        return request.getParam().stream()
                .collect(Collectors.toMap(Param::getName, Param::getData));
    }

    private HttpEntity getBody(final ElasticSearchRequest request) {
        if (!(request instanceof ElasticSearchRequestWithBody)) {
            return null;
        }
        ElasticSearchRequestWithBody requestWithBody = (ElasticSearchRequestWithBody) request;
        Body body = requestWithBody.getBody();
        return HttpUtil.extractBody(body, true, this, dependencies);
    }
}
