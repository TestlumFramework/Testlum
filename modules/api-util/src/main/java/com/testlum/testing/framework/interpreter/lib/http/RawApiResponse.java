package com.testlum.testing.framework.interpreter.lib.http;

import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the raw HTTP response received after making an API call.
 * The class ensures immutability by copying the input headers and body during initialization
 * and when accessed via getters.
 * <p>
 * One of the main reasons to use this class instead of {@link ApiResponse} is to save
 * proxy-fidelity for binary payload. Because of this scenario, it's been used mainly
 * for the inbound module.
 */

public class RawApiResponse {

    @Getter
    private final int code;
    // List as value to allow duplication of the headers
    @Getter
    private final Map<String, List<String>> headers;
    private final byte[] body;

    public RawApiResponse(final int code, final Map<String, List<String>> headers, final byte[] body) {
        this.code = code;
        this.headers = headers == null ? Map.of() : copy(headers);
        this.body = body == null ? new byte[0] : body.clone();
    }

    private static Map<String, List<String>> copy(final Map<String, List<String>> sourceHeaders) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        sourceHeaders.forEach((k, v) -> headers.put(k, new ArrayList<>(v)));
        return Map.copyOf(headers);
    }

    public byte[] getBody() {
        return body.clone();
    }
}
