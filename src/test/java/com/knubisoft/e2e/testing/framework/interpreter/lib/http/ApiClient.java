package com.knubisoft.e2e.testing.framework.interpreter.lib.http;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.util.HttpUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.HTTP_STATUS_CODE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.UNKNOWN_METHOD;
import static java.lang.String.format;

@Slf4j
@Component
public class ApiClient {

    public ApiResponse call(final HttpMethod httpMethod,
                            final String url,
                            final Map<String, String> headers,
                            final HttpEntity body,
                            final String alias) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            return executeHttpRequest(httpMethod, url, headers, body, httpClient, alias);
        }
    }

    private ApiResponse executeHttpRequest(final HttpMethod httpMethod,
                                           final String url,
                                           final Map<String, String> headers,
                                           final HttpEntity body,
                                           final CloseableHttpClient httpClient,
                                           final String alias) throws IOException {
        HttpUriRequest request = buildRequest(httpMethod, url, headers, body, alias);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return convertToApiResponse(response);
        }
    }

    private ApiResponse convertToApiResponse(final CloseableHttpResponse response) throws IOException {
        Map<String, String> responseHeaders = new LinkedHashMap<>();
        for (Header each : response.getAllHeaders()) {
            responseHeaders.put(each.getName(), each.getValue());
        }
        HttpEntity entity = response.getEntity();
        Object responseBody = entity == null ? StringUtils.EMPTY : httpEntityToResponseBody(entity);
        log.info(HTTP_STATUS_CODE, response.getStatusLine().getStatusCode(),
                response.getStatusLine().getReasonPhrase());
        return new ApiResponse(response.getStatusLine().getStatusCode(),
                responseHeaders, responseBody);
    }

    private HttpUriRequest buildRequest(final HttpMethod httpMethod,
                                        final String url,
                                        final Map<String, String> headers,
                                        final HttpEntity body,
                                        final String alias) {
        HttpUriRequest request = getHttpRequest(httpMethod, createFullURL(url, alias), body);
        addRequestHeaders(request, headers);
        return request;
    }

    //CHECKSTYLE:OFF
    private HttpUriRequest getHttpRequest(final HttpMethod httpMethod,
                                          final String uri,
                                          final HttpEntity body) {
        switch (httpMethod) {
            case GET:
                return new HttpGet(uri);
            case DELETE:
                return new HttpDelete(uri);
            case HEAD:
                return new HttpHead(uri);
            case OPTIONS:
                return new HttpOptions(uri);
            case POST:
                return setRequestBody(new HttpPost(uri), body);
            case PUT:
                return setRequestBody(new HttpPut(uri), body);
            case PATCH:
                return setRequestBody(new HttpPatch(uri), body);
            case TRACE:
                return new HttpTrace(uri);
            default:
                throw new DefaultFrameworkException(UNKNOWN_METHOD, httpMethod);
        }
    }
    //CHECKSTYLE:ON

    private String createFullURL(final String url, final String alias) {
        String apiUrl = GlobalTestConfigurationProvider.getIntegrations().getApis().getApi()
                .stream()
                .filter(o -> o.getAlias().equalsIgnoreCase(alias))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(format("Cannot find api with alias \"%s\"", alias)))
                .getUrl();
        return apiUrl + url;
    }

    private void addRequestHeaders(final HttpUriRequest request, final Map<String, String> headers) {
        headers.putIfAbsent(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        for (Map.Entry<String, String> each : headers.entrySet()) {
            request.addHeader(each.getKey(), each.getValue());
        }
    }

    private HttpUriRequest setRequestBody(final HttpEntityEnclosingRequestBase request,
                                          final HttpEntity body) {
        request.setEntity(body);
        return request;
    }

    @SneakyThrows
    private Object httpEntityToResponseBody(final HttpEntity httpEntity) {
        if (HttpUtil.checkIfContentTypeIsJson(httpEntity.getContentType())) {
            return new JSONParser().parse(EntityUtils.toString(httpEntity));
        }
        return EntityUtils.toString(httpEntity);
    }
}
