package com.testlum.testing.framework.inbound.server.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Transport-agnostic representation of an inbound HTTP response.
 * <p>
 * The response is immutable, ensuring that the contained data cannot be modified once
 * the instance is created. Headers are stored as a case-insensitive unmodifiable map
 * to ensure proper adherence to HTTP header standards.
 */
public class InboundResponse {

    private final int status;
    private final Map<String, List<String>> headers;
    private final byte[] body;

    public InboundResponse(final int status, final Map<String, List<String>> headers, final byte[] body) {
        this.status = status;
        TreeMap<String, List<String>> caseInsensitiveHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (headers != null) {
            caseInsensitiveHeaders.putAll(headers);
        }
        this.headers = Collections.unmodifiableSortedMap(caseInsensitiveHeaders);
        this.body = body == null ? new byte[0] : body.clone();
    }

    public int getStatus() {
        return status;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body.clone();
    }

}
