package com.knubisoft.testlum.testing.framework.inbound.server.util;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InboundDtoMapper {

    public InboundRequest toRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String rawQuery = exchange.getRequestURI().getRawQuery();

        Map<String, List<String>> headers = fetchRequestHeaders(exchange);

        byte[] body;
        try (InputStream inputStream = exchange.getRequestBody()) {
            body = inputStream.readAllBytes();
        }

        return new InboundRequest(method, path, rawQuery, headers,
                parseQuery(rawQuery), body);

    }

    private Map<String, List<String>> fetchRequestHeaders(HttpExchange exchange) {
        Map<String, List<String>> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> header : exchange.getRequestHeaders().entrySet()) {
            headers.put(header.getKey(), header.getValue());
        }
        return headers;
    }

    private Map<String, List<String>> parseQuery(String rawQuery) {
        Map<String, List<String>> queryParams = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return queryParams;
        }
        for (String pair : rawQuery.split("&")) {
            int equalitySymbolIndex = pair.indexOf('=');
            String key = equalitySymbolIndex >= 0 ? pair.substring(0, equalitySymbolIndex) : pair;
            String value = equalitySymbolIndex >= 0 ? pair.substring(equalitySymbolIndex + 1) : "";
            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            value = URLDecoder.decode(value, StandardCharsets.UTF_8);
            queryParams.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return queryParams;
    }

}
