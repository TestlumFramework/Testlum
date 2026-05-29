package com.knubisoft.testlum.testing.framework.inbound.server.util;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.io.ByteArrayInputStream;
import java.net.URI;
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

    /**
     * AND-combination of matchers. The request must match all matchers.
     * <p>
     * It's used to build a compound correlation key when any single matcher
     * could not fully satisfy the match condition.
     */
    static CorrelationMatcher combined(CorrelationMatcher... matchers) {
        if (matchers == null || matchers.length == 0) {
            return always();
        }
        if (matchers.length == 1) {
            return matchers[0];
        }
        CorrelationMatcher[] copy = matchers.clone();
        return request -> {
            for (CorrelationMatcher matcher : copy) {
                if (!matcher.matches(request)) {
                    return false;
                }
            }
            return true;
        };
    }

    static CorrelationMatcher combined(List<CorrelationMatcher> matchers) {
        if (matchers == null || matchers.isEmpty()) {
            return always();
        }
        if (matchers.size() == 1) {
            return matchers.get(0);
        }
        return combined(matchers.toArray(new CorrelationMatcher[0]));
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

    static CorrelationMatcher endpoint(String url) {
        String path = extractPath(url);
        return request -> path.equals(request.getPath());
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

    private static String extractPath(String url) {
        if (url.startsWith("/")) {
            return url;
        }
        try {
            URI uri = URI.create(url);
            String path = uri.getPath();
            return (path == null || path.isEmpty()) ? "/" : path;
        } catch (IllegalArgumentException e) {
            return url;
        }
    }
}
