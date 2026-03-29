package com.knubisoft.testlum.testing.framework.interpreter.lib.http;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void constructorAndGetters() {
        final Map<String, String> headers = Map.of("Content-Type", "application/json");
        final ApiResponse response = new ApiResponse(200, headers, "{\"ok\":true}");

        assertEquals(200, response.getCode());
        assertEquals(headers, response.getHeaders());
        assertEquals("{\"ok\":true}", response.getBody());
    }

    @Test
    void errorResponse() {
        final ApiResponse response = new ApiResponse(500, Map.of(), "Internal Server Error");

        assertEquals(500, response.getCode());
        assertEquals("Internal Server Error", response.getBody());
    }

    @Nested
    class EdgeCases {
        @Test
        void nullHeaders() {
            final ApiResponse response = new ApiResponse(200, null, "body");
            assertNull(response.getHeaders());
        }

        @Test
        void nullBody() {
            final ApiResponse response = new ApiResponse(200, Map.of(), null);
            assertNull(response.getBody());
        }

        @Test
        void emptyBody() {
            final ApiResponse response = new ApiResponse(200, Map.of(), "");
            assertEquals("", response.getBody());
        }

        @Test
        void emptyHeaders() {
            final ApiResponse response = new ApiResponse(200, Collections.emptyMap(), "body");
            assertTrue(response.getHeaders().isEmpty());
        }

        @Test
        void multipleHeaders() {
            final Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer token");
            headers.put("X-Request-Id", "123");

            final ApiResponse response = new ApiResponse(200, headers, "body");
            assertEquals(3, response.getHeaders().size());
            assertEquals("Bearer token", response.getHeaders().get("Authorization"));
        }

        @Test
        void variousStatusCodes() {
            assertEquals(201, new ApiResponse(201, Map.of(), "").getCode());
            assertEquals(301, new ApiResponse(301, Map.of(), "").getCode());
            assertEquals(400, new ApiResponse(400, Map.of(), "").getCode());
            assertEquals(403, new ApiResponse(403, Map.of(), "").getCode());
            assertEquals(404, new ApiResponse(404, Map.of(), "").getCode());
            assertEquals(502, new ApiResponse(502, Map.of(), "").getCode());
        }

        @Test
        void largeBody() {
            final String largeBody = "x".repeat(10000);
            final ApiResponse response = new ApiResponse(200, Map.of(), largeBody);
            assertEquals(10000, response.getBody().length());
        }
    }
}
