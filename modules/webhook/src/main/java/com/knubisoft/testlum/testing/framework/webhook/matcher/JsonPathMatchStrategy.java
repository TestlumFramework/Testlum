package com.knubisoft.testlum.testing.framework.webhook.matcher;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;

import java.io.ByteArrayInputStream;
import java.util.Objects;

public class JsonPathMatchStrategy implements WebhookMatchStrategy {

    private static final String JSON_PATH_ROOT = "$";
    private final String jsonPath;
    private final String expected;

    public JsonPathMatchStrategy(final String jsonKey, final String expected) {
        this.jsonPath = jsonKey;
        this.expected = expected;
    }

    @Override
    public boolean matches(InboundRequest request) {
        try {
            DocumentContext contextBody = JsonPath.parse(new ByteArrayInputStream(request.getBody()));
            Object actual = contextBody.read(toJsonPath(jsonPath));
            return Objects.nonNull(actual) && expected.equals(actual.toString());
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static String toJsonPath(final String key) {
        return key.startsWith(JSON_PATH_ROOT) ? key : JSON_PATH_ROOT + "." + key;
    }
}
