package com.knubisoft.e2e.testing.framework.interpreter.lib.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ApiResponse {
    private final int code;
    private final Map<String, String> headers;
    private final Object body;
}
