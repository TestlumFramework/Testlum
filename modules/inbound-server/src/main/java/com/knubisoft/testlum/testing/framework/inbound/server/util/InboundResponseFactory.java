package com.knubisoft.testlum.testing.framework.inbound.server.util;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class InboundResponseFactory {

    public static InboundResponse json(int status, String jsonBody) {
        return new InboundResponse(
                status,
                Map.of("Content-Type", List.of("application/json; charset=utf-8")),
                jsonBody.getBytes(StandardCharsets.UTF_8));
    }

    public static InboundResponse of(int status, Map<String, List<String>> headers, byte[] body) {
        return new InboundResponse(
                status,
                headers,
                body);
    }

    public static InboundResponse internalError(String message) {
        return json(500, "{\"error\":\"" + escape(message) + "\"}");
    }

    public static InboundResponse notFound(String message) {
        return json(404, "{\"error\":\"" + escape(message) + "\"}");
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
