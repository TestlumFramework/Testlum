package com.knubisoft.testlum.testing.framework.interpreter.lib.http;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
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
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.DisposableBean;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class ApiClient implements DisposableBean {

    private static final String HTTP_STATUS_CODE = LogFormat.table("Status code", "{} {}");
    private static final int MAX_TOTAL_CONNECTIONS = 50;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 10;

    private final CloseableHttpClient httpClient;

    public ApiClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        connectionManager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
    }

    @Override
    public void destroy() throws IOException {
        httpClient.close();
    }

    public ApiResponse call(final HttpMethod httpMethod,
                            final String url,
                            final Map<String, String> headers,
                            final HttpEntity body) throws Exception {
        HttpUriRequest request = buildRequest(httpMethod, url, headers, body);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return readAPIResponse(response);
        }
    }

    private ApiResponse readAPIResponse(final CloseableHttpResponse response) throws Exception {
        Map<String, String> responseHeaders = new LinkedHashMap<>();
        for (Header each : response.getAllHeaders()) {
            responseHeaders.put(each.getName(), each.getValue());
        }
        HttpEntity entity = response.getEntity();
        String responseBody = Objects.isNull(entity) ? StringUtils.EMPTY : EntityUtils.toString(entity);
        log.info(HTTP_STATUS_CODE, response.getStatusLine().getStatusCode(),
                response.getStatusLine().getReasonPhrase());
        return new ApiResponse(response.getStatusLine().getStatusCode(), responseHeaders, responseBody);
    }

    private HttpUriRequest buildRequest(final HttpMethod httpMethod,
                                        final String url,
                                        final Map<String, String> headers,
                                        final HttpEntity body) {
        HttpUriRequest request = getHttpRequest(httpMethod, url, body);
        addRequestHeaders(request, headers);
        return request;
    }

    // CHECKSTYLE:OFF
    private HttpUriRequest getHttpRequest(final HttpMethod httpMethod,
                                          final String url,
                                          final HttpEntity body) {
        if (httpMethod.equals(HttpMethod.GET)) {
            return new HttpGet(url);
        } else if (httpMethod.equals(HttpMethod.DELETE)) {
            return new HttpDelete(url);
        } else if (httpMethod.equals(HttpMethod.HEAD)) {
            return new HttpHead(url);
        } else if (httpMethod.equals(HttpMethod.OPTIONS)) {
            return new HttpOptions(url);
        } else if (httpMethod.equals(HttpMethod.TRACE)) {
            return new HttpTrace(url);
        } else if (httpMethod.equals(HttpMethod.POST)) {
            return setRequestBody(new HttpPost(url), body);
        } else if (httpMethod.equals(HttpMethod.PUT)) {
            return setRequestBody(new HttpPut(url), body);
        } else if (httpMethod.equals(HttpMethod.PATCH)) {
            return setRequestBody(new HttpPatch(url), body);
        }
        throw new DefaultFrameworkException("Unknown http method: %s", httpMethod);
    }
    // CHECKSTYLE:ON

    private void addRequestHeaders(final HttpUriRequest request,
                                   final Map<String, String> headers) {
        for (Map.Entry<String, String> each : headers.entrySet()) {
            request.addHeader(each.getKey(), each.getValue());
        }
    }

    private HttpUriRequest setRequestBody(final HttpEntityEnclosingRequestBase request,
                                          final HttpEntity body) {
        request.setEntity(body);
        return request;
    }
}