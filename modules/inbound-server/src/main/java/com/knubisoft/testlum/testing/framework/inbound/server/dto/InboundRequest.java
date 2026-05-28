package com.knubisoft.testlum.testing.framework.inbound.server.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Transport-agnostic representation of an inbound HTTP request.
 * <p>
 * We keep body as raw bytes as we don't have rights for any normalization.
 * Only the handler should parse the body.
 * <p>
 * Instances of this class are immutable, with headers and query parameters provided
 * as unmodifiable maps to prevent tampering.
 */
public class InboundRequest {

    private final String method;
    private final String path;
    private final String rawQuery;
    private final Map<String, List<String>> headers;
    private final Map<String, List<String>> queryParams;
    private final byte[] body;

    public InboundRequest(String method,
                          String path,
                          String rawQuery,
                          Map<String, List<String>> headers,
                          Map<String, List<String>> queryParams,
                          byte[] body) {
        this.method = method;
        this.path = path;
        this.rawQuery = rawQuery;
        // Because HTTP headers are case-insensitive. So, we need case-insensitive lookup.
        // For example, "X-Test-Id" has to be found with get("x-test-id").
        TreeMap<String, List<String>> caseInsensitiveHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (headers != null) {
            caseInsensitiveHeaders.putAll(headers);
        }
        // To restrict modification of the headers.
        this.headers = Collections.unmodifiableSortedMap(caseInsensitiveHeaders);
        this.queryParams = queryParams == null ? Map.of() : Map.copyOf(queryParams);
        this.body = body == null ? new byte[0] : body.clone();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Optional<String> getRawQuery() {
        return Optional.ofNullable(rawQuery);
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public byte[] getBody() {
        return body.clone();
    }

    @Override
    public String toString() {
        return method + " " + path + (rawQuery == null ? "" : "?" + rawQuery)
                + " (" + body.length + " bytes)";
    }
}
