package com.testlum.testing.framework.forwarder;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.testlum.testing.framework.inbound.server.util.InboundResponseFactory;
import com.testlum.testing.framework.interpreter.lib.http.ApiClient;
import com.testlum.testing.framework.interpreter.lib.http.RawApiResponse;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DestinationForwarder {

    private static final Set<String> SKIP_HEADERS = new HashSet<>(Arrays.asList(
            "host",
            "content-length",
            "connection",
            "transfer-encoding",
            "keep-alive",
            "upgrade",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "expect"
    ));

    private final ApiClient apiClient;

    public DestinationForwarder(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public InboundResponse forward(InboundRequest request, String destinationUrl) {
        String url = buildTargetUrl(request, destinationUrl);
        Map<String, String> headers = prepareHeaders(request);

        Object body = mapToHttpEntity(request.getBody(), getContentType(request).orElse(null));

        // Log forwarding

        RawApiResponse response;

        try {
            HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
            response = apiClient.callRaw(httpMethod, url, headers,  (HttpEntity) body);
        } catch (Exception e) {
            throw new DefaultFrameworkException("Forwarding to destination " + url + " failed: " + e.getMessage(), e);
        }
        return mapToInboundResponse(response);
    }

    private String buildTargetUrl(InboundRequest request, String destinationUrl) {
        if (request.getRawQuery().isPresent() && !destinationUrl.contains("?")) {
            return destinationUrl + "?" + request.getRawQuery().get();
        }
        return destinationUrl;
    }

    private Map<String, String> prepareHeaders(InboundRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        request.getHeaders().forEach((name, values) -> {
            if (SKIP_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                return;
            }
            if (values == null || values.isEmpty()) {
                return;
            }
            headers.put(name, values.size() == 1 ? values.get(0) : String.join(",", values));
        });
        headers.put("X-Forwarded-By", "Testlum");
        return headers;
    }

    private Optional<String> getContentType(InboundRequest request) {
        return request.getHeaders().containsKey("Content-Type") ?
                Optional.of(request.getHeaders().get("Content-Type").get(0))
                : Optional.empty();
    }

    private Object mapToHttpEntity(byte[] body, String contentType) {
        if (body == null || body.length == 0) {
            return null;
        }
        ContentType type = (contentType == null) ? null : ContentType.parse(contentType);
        return (type == null) ? new ByteArrayEntity(body) : new ByteArrayEntity(body, type);
    }

    private InboundResponse mapToInboundResponse(RawApiResponse response) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        response.getHeaders().forEach((name, values) -> {
            if (!SKIP_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                headers.put(name, new ArrayList<>(values));
            }
        });
        return InboundResponseFactory.of(response.getCode(), headers, response.getBody());
    }
}
