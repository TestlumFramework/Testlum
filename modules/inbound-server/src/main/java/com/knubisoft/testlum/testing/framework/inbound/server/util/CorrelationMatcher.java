package com.knubisoft.testlum.testing.framework.inbound.server.util;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Functional interface for matching {@link InboundRequest} objects based on specific criteria.
 * This interface provides a contract for creating and applying correlation matchers which evaluate
 * provided {@link InboundRequest} instances against defined conditions.
 * <p>
 * It's used to map incoming requests to corresponding test scenarios with proper correlation identifier
 * within parallel execution.
 */
@FunctionalInterface
public interface CorrelationMatcher {

    boolean matches(InboundRequest request);

    /**
     * For testing without parallel execution.
     */
    static CorrelationMatcher always() {
        return request -> true;
    }

    static CorrelationMatcher header(String name, String expected, boolean contains) {
        return request -> {
            Map<String, List<String>> valuesMap = request.getHeaders();
            return isValuePresent(name, expected, contains, valuesMap);
        };
    }

    static CorrelationMatcher queryParam(String name, String expected, boolean contains) {
        return request -> {
            Map<String, List<String>> valuesMap = request.getQueryParams();
            return isValuePresent(name, expected, contains, valuesMap);
        };
    }

    static CorrelationMatcher jsonPath(String jsonPath, String expected) {
        return request -> {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(request.getBody());
            DocumentContext contextBody = JsonPath.parse(inputStream);
            if (Objects.isNull(contextBody.read(jsonPath))) {
                return false;
            }
            return expected.equals(contextBody.read(jsonPath).toString());
        };
    }

    private static boolean isValuePresent(String name, String expected, boolean contains, Map<String, List<String>> valuesMap) {
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
