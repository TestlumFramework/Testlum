package com.testlum.testing.framework.webhook.matcher;

import com.testlum.testing.framework.inbound.server.dto.InboundRequest;

import java.util.List;
import java.util.Map;

public class HeaderMatchStrategy implements WebhookMatchStrategy {

    private final String name;
    private final String expected;
    private final boolean contains;

    public HeaderMatchStrategy(final String name, final String expected, final boolean contains) {
        this.name = name;
        this.expected = expected;
        this.contains = contains;
    }

    @Override
    public boolean matches(InboundRequest request) {
        Map<String, List<String>> valuesMap = request.getHeaders();
        return isValuePresent(name, expected, contains, valuesMap);
    }
}
