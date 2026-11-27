package com.testlum.testing.framework.webhook.matcher;

import com.testlum.testing.framework.inbound.server.dto.InboundRequest;

import java.util.List;
import java.util.Map;

public interface WebhookMatchStrategy {

    boolean matches(InboundRequest request);

    default boolean isValuePresent(String name, String expected, boolean contains, Map<String, List<String>> valuesMap) {
        List<String> values = valuesMap.get(name);
        if (values == null || values.isEmpty()) {
            return false;
        }
        if (contains) {
            return values.stream().anyMatch(value -> value.contains(expected));
        }
        return values.stream().anyMatch(value -> value.equals(expected));
    }
}
