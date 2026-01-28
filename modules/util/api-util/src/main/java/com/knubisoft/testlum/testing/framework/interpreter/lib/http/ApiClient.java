package com.knubisoft.testlum.testing.framework.interpreter.lib.http;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;

import com.knubisoft.testlum.testing.framework.interpreter.lib.http.util.HttpUtil;
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
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static org.springframework.http.HttpMethod.*;

@Slf4j
@Component
public class ApiClient {

    public static final String UNKNOWN_HTTP_METHOD = "Unknown http method: %s";
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String HTTP_STATUS_CODE = format(TABLE_FORMAT, "Status code", "{} {}");

    public ApiResponse call(final HttpMethod httpMethod,
                            final String url,
                            final Map<String, String> headers,
                            final HttpEntity body) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            return executeHttpRequest(httpMethod, url, headers, body, httpClient);
        }
    }

    private ApiResponse executeHttpRequest(final HttpMethod httpMethod,
                                           final String url,
                                           final Map<String, String> headers,
                                           final HttpEntity body,
                                           final CloseableHttpClient httpClient) throws Exception {
        HttpUriRequest request = buildRequest(httpMethod, url, headers, body);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return convertToApiResponse(response);
        }
    }

    private ApiResponse convertToApiResponse(final CloseableHttpResponse response) throws Exception {
        Map<String, String> responseHeaders = new LinkedHashMap<>();
        for (Header each : response.getAllHeaders()) {
            responseHeaders.put(each.getName(), each.getValue());
        }
        HttpEntity entity = response.getEntity();
        Object responseBody = Objects.isNull(entity) ? StringUtils.EMPTY : httpEntityToResponseBody(entity);
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
        if (httpMethod.equals(GET)) {
            return new HttpGet(url);
        } else if (httpMethod.equals(DELETE)) {
            return new HttpDelete(url);
        } else if (httpMethod.equals(HEAD)) {
            return new HttpHead(url);
        } else if (httpMethod.equals(OPTIONS)) {
            return new HttpOptions(url);
        } else if (httpMethod.equals(TRACE)) {
            return new HttpTrace(url);
        } else if (httpMethod.equals(POST)) {
            return setRequestBody(new HttpPost(url), body);
        } else if (httpMethod.equals(PUT)) {
            return setRequestBody(new HttpPut(url), body);
        } else if (httpMethod.equals(PATCH)) {
            return setRequestBody(new HttpPatch(url), body);
        }
        throw new DefaultFrameworkException(UNKNOWN_HTTP_METHOD, httpMethod);
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

    private Object httpEntityToResponseBody(final HttpEntity httpEntity) throws Exception {
        if (HttpUtil.checkIfContentTypeIsJson(httpEntity.getContentType())) {
            return new JSONParser().parse(EntityUtils.toString(httpEntity));
        }
        return EntityUtils.toString(httpEntity);
    }
}