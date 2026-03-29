package com.knubisoft.testlum.testing.framework.interpreter.lib.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApiClientTest {

    private ApiClient apiClient;

    @BeforeEach
    void setUp() {
        apiClient = new ApiClient();
    }

    @AfterEach
    void tearDown() throws IOException {
        apiClient.destroy();
    }

    @Test
    void constructorCreatesClient() {
        assertNotNull(apiClient);
    }

    @Test
    void destroyDoesNotThrow() {
        assertDoesNotThrow(() -> apiClient.destroy());
    }

    @Test
    void destroyCanBeCalledMultipleTimes() {
        assertDoesNotThrow(() -> {
            apiClient.destroy();
            apiClient.destroy();
        });
    }

    @Nested
    class BuildRequest {

        private HttpUriRequest invokeGetHttpRequest(final HttpMethod method,
                                                       final String url,
                                                       final HttpEntity body) throws Exception {
            Method m = ApiClient.class.getDeclaredMethod(
                    "getHttpRequest", HttpMethod.class, String.class, HttpEntity.class);
            m.setAccessible(true);
            return (HttpUriRequest) m.invoke(apiClient, method, url, body);
        }

        @Test
        void getMethodReturnsHttpGet() throws Exception {
            HttpUriRequest request = invokeGetHttpRequest(HttpMethod.GET, "http://localhost/api", null);
            assertInstanceOf(HttpGet.class, request);
            assertEquals("http://localhost/api", request.getURI().toString());
        }

        @Test
        void deleteMethodReturnsHttpDelete() throws Exception {
            HttpUriRequest request = invokeGetHttpRequest(HttpMethod.DELETE, "http://localhost/api/1", null);
            assertInstanceOf(HttpDelete.class, request);
            assertEquals("http://localhost/api/1", request.getURI().toString());
        }

        @Test
        void headMethodReturnsHttpHead() throws Exception {
            HttpUriRequest request = invokeGetHttpRequest(HttpMethod.HEAD, "http://localhost/api", null);
            assertInstanceOf(HttpHead.class, request);
        }

        @Test
        void optionsMethodReturnsHttpOptions() throws Exception {
            HttpUriRequest request = invokeGetHttpRequest(HttpMethod.OPTIONS, "http://localhost/api", null);
            assertInstanceOf(HttpOptions.class, request);
        }

        @Test
        void traceMethodReturnsHttpTrace() throws Exception {
            HttpUriRequest request = invokeGetHttpRequest(HttpMethod.TRACE, "http://localhost/api", null);
            assertInstanceOf(HttpTrace.class, request);
        }

        @Test
        void postMethodReturnsHttpPost() throws Exception {
            HttpEntity body = new StringEntity("{}", org.apache.http.entity.ContentType.APPLICATION_JSON);
            HttpUriRequest request = invokeGetHttpRequest(HttpMethod.POST, "http://localhost/api", body);
            assertInstanceOf(HttpPost.class, request);
            assertNotNull(((HttpPost) request).getEntity());
        }

        @Test
        void putMethodReturnsHttpPut() throws Exception {
            HttpEntity body = new StringEntity("{}", org.apache.http.entity.ContentType.APPLICATION_JSON);
            HttpUriRequest request = invokeGetHttpRequest(HttpMethod.PUT, "http://localhost/api/1", body);
            assertInstanceOf(HttpPut.class, request);
            assertNotNull(((HttpPut) request).getEntity());
        }

        @Test
        void patchMethodReturnsHttpPatch() throws Exception {
            HttpEntity body = new StringEntity("{}", org.apache.http.entity.ContentType.APPLICATION_JSON);
            HttpUriRequest request = invokeGetHttpRequest(HttpMethod.PATCH, "http://localhost/api/1", body);
            assertInstanceOf(HttpPatch.class, request);
            assertNotNull(((HttpPatch) request).getEntity());
        }

        @Test
        void postWithNullBodySetsNullEntity() throws Exception {
            HttpUriRequest request = invokeGetHttpRequest(HttpMethod.POST, "http://localhost/api", null);
            assertInstanceOf(HttpPost.class, request);
        }
    }

    @Nested
    class AddHeaders {

        private HttpUriRequest invokeBuildRequest(final HttpMethod method, final String url,
                                                   final Map<String, String> headers,
                                                   final HttpEntity body) throws Exception {
            Method m = ApiClient.class.getDeclaredMethod("buildRequest",
                    HttpMethod.class, String.class, Map.class, HttpEntity.class);
            m.setAccessible(true);
            return (HttpUriRequest) m.invoke(apiClient, method, url, headers, body);
        }

        @Test
        void headersAreAddedToRequest() throws Exception {
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Authorization", "Bearer token");
            headers.put("Accept", "application/json");

            HttpUriRequest request = invokeBuildRequest(HttpMethod.GET, "http://localhost/api", headers, null);

            assertEquals("Bearer token", request.getFirstHeader("Authorization").getValue());
            assertEquals("application/json", request.getFirstHeader("Accept").getValue());
        }

        @Test
        void emptyHeadersMap() throws Exception {
            Map<String, String> headers = new LinkedHashMap<>();
            HttpUriRequest request = invokeBuildRequest(HttpMethod.GET, "http://localhost/api", headers, null);
            assertEquals(0, request.getAllHeaders().length);
        }

        @Test
        void multipleHeadersPreserved() throws Exception {
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("X-Header-1", "val1");
            headers.put("X-Header-2", "val2");
            headers.put("X-Header-3", "val3");

            HttpUriRequest request = invokeBuildRequest(HttpMethod.GET, "http://localhost/api", headers, null);
            assertEquals(3, request.getAllHeaders().length);
        }
    }

    @Nested
    class CallMethod {

        @Test
        void callReturnsApiResponseOnSuccess() throws Exception {
            CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
            CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
            StatusLine statusLine = mock(StatusLine.class);

            when(statusLine.getStatusCode()).thenReturn(200);
            when(statusLine.getReasonPhrase()).thenReturn("OK");
            when(mockResponse.getStatusLine()).thenReturn(statusLine);
            when(mockResponse.getAllHeaders()).thenReturn(new Header[0]);
            when(mockResponse.getEntity()).thenReturn(new StringEntity("{\"ok\":true}"));
            when(mockClient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);

            setHttpClient(mockClient);

            Map<String, String> headers = new LinkedHashMap<>();
            ApiResponse response = apiClient.call(HttpMethod.GET, "http://localhost/api", headers, null);

            assertEquals(200, response.getCode());
            assertEquals("{\"ok\":true}", response.getBody());
        }

        @Test
        void callWithNullEntityReturnsEmptyBody() throws Exception {
            CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
            CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
            StatusLine statusLine = mock(StatusLine.class);

            when(statusLine.getStatusCode()).thenReturn(204);
            when(statusLine.getReasonPhrase()).thenReturn("No Content");
            when(mockResponse.getStatusLine()).thenReturn(statusLine);
            when(mockResponse.getAllHeaders()).thenReturn(new Header[0]);
            when(mockResponse.getEntity()).thenReturn(null);
            when(mockClient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);

            setHttpClient(mockClient);

            Map<String, String> headers = new LinkedHashMap<>();
            ApiResponse response = apiClient.call(HttpMethod.DELETE, "http://localhost/api/1", headers, null);

            assertEquals(204, response.getCode());
            assertEquals("", response.getBody());
        }

        @Test
        void callReturnsResponseHeaders() throws Exception {
            CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
            CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
            StatusLine statusLine = mock(StatusLine.class);

            Header header1 = mock(Header.class);
            when(header1.getName()).thenReturn("Content-Type");
            when(header1.getValue()).thenReturn("application/json");
            Header header2 = mock(Header.class);
            when(header2.getName()).thenReturn("X-Request-Id");
            when(header2.getValue()).thenReturn("abc-123");

            when(statusLine.getStatusCode()).thenReturn(200);
            when(statusLine.getReasonPhrase()).thenReturn("OK");
            when(mockResponse.getStatusLine()).thenReturn(statusLine);
            when(mockResponse.getAllHeaders()).thenReturn(new Header[]{header1, header2});
            when(mockResponse.getEntity()).thenReturn(new StringEntity("body"));
            when(mockClient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);

            setHttpClient(mockClient);

            Map<String, String> headers = new LinkedHashMap<>();
            ApiResponse response = apiClient.call(HttpMethod.GET, "http://localhost/api", headers, null);

            assertEquals("application/json", response.getHeaders().get("Content-Type"));
            assertEquals("abc-123", response.getHeaders().get("X-Request-Id"));
        }

        @Test
        void callWithPostAndBody() throws Exception {
            CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
            CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
            StatusLine statusLine = mock(StatusLine.class);

            when(statusLine.getStatusCode()).thenReturn(201);
            when(statusLine.getReasonPhrase()).thenReturn("Created");
            when(mockResponse.getStatusLine()).thenReturn(statusLine);
            when(mockResponse.getAllHeaders()).thenReturn(new Header[0]);
            when(mockResponse.getEntity()).thenReturn(new StringEntity("{\"id\":1}"));
            when(mockClient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);

            setHttpClient(mockClient);

            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-Type", "application/json");
            HttpEntity body = new StringEntity("{\"name\":\"test\"}");
            ApiResponse response = apiClient.call(HttpMethod.POST, "http://localhost/api", headers, body);

            assertEquals(201, response.getCode());
            assertEquals("{\"id\":1}", response.getBody());
        }

        @Test
        void callPropagatesException() throws Exception {
            CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
            when(mockClient.execute(any(HttpUriRequest.class))).thenThrow(new IOException("Connection refused"));

            setHttpClient(mockClient);

            Map<String, String> headers = new LinkedHashMap<>();
            assertThrows(IOException.class,
                    () -> apiClient.call(HttpMethod.GET, "http://localhost/api", headers, null));
        }

        @Test
        void callClosesResponse() throws Exception {
            CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
            CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
            StatusLine statusLine = mock(StatusLine.class);

            when(statusLine.getStatusCode()).thenReturn(200);
            when(statusLine.getReasonPhrase()).thenReturn("OK");
            when(mockResponse.getStatusLine()).thenReturn(statusLine);
            when(mockResponse.getAllHeaders()).thenReturn(new Header[0]);
            when(mockResponse.getEntity()).thenReturn(null);
            when(mockClient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);

            setHttpClient(mockClient);

            Map<String, String> headers = new LinkedHashMap<>();
            apiClient.call(HttpMethod.GET, "http://localhost/api", headers, null);

            verify(mockResponse).close();
        }

        private void setHttpClient(final CloseableHttpClient mockClient) throws Exception {
            Field field = ApiClient.class.getDeclaredField("httpClient");
            field.setAccessible(true);
            field.set(apiClient, mockClient);
        }
    }
}
