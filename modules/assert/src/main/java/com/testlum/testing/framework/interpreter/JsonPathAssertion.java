package com.testlum.testing.framework.interpreter;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.testlum.testing.model.scenario.AssertJsonPath;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Checks one field of a JSON body: exists, equals a value, matches a regex. */
final class JsonPathAssertion {

    private static final String FAILURE = "%s: %s";
    private static final String NO_BODY = "%s: there is no response body to read";
    private static final String NOT_JSON = "%s: the body is not valid JSON: %s";
    private static final String CANNOT_READ_PATH = "%s: cannot read path: %s";
    private static final String NOT_FOUND = "not found";
    private static final String EXISTS_BUT_SHOULD_NOT = "exists with value [%s] but should not";
    private static final String WRONG_VALUE = "expected [%s] but was [%s]";
    private static final String NO_MATCH = "[%s] does not match [%s]";
    private static final String CONTRADICTION = "%s: exists=false cannot be combined with equals or matches";
    private static final String BAD_REGEX = "matches is not a valid regex: %s";
    // trailing text after the first value would otherwise be ignored, and `1,2` would read as 1
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).build();

    private final AssertJsonPath assertion;
    private JsonPath path;
    private Object value;
    private boolean found = true;

    private JsonPathAssertion(final AssertJsonPath assertion) {
        this.assertion = assertion;
    }

    /** Returns the failure message, or null when every given check holds. */
    static String check(final String body, final AssertJsonPath assertion) {
        return new JsonPathAssertion(assertion).run(body);
    }

    private String run(final String body) {
        String unreadable = unreadable(body);
        if (unreadable != null) {
            return unreadable;
        }
        List<String> failures = new ArrayList<>(existence());
        if (found) {
            failures.addAll(valueChecks());
        }
        return failures.isEmpty() ? null : String.format(FAILURE, assertion.getPath(), String.join("; ", failures));
    }

    private String unreadable(final String body) {
        String problem = contradiction();
        if (problem == null) {
            problem = compilePath();
        }
        return problem == null ? read(body) : problem;
    }

    private String contradiction() {
        boolean absent = Boolean.FALSE.equals(assertion.isExists());
        boolean wantsValue = assertion.getEquals() != null || assertion.getMatches() != null;
        return absent && wantsValue ? String.format(CONTRADICTION, assertion.getPath()) : null;
    }

    private String compilePath() {
        try {
            path = JsonPath.compile(assertion.getPath());
        } catch (RuntimeException e) {
            return String.format(CANNOT_READ_PATH, assertion.getPath(), firstLine(e));
        }
        return null;
    }

    private String read(final String body) {
        if (body == null || body.isBlank()) {
            return String.format(NO_BODY, assertion.getPath());
        }
        try {
            value = MAPPER.readValue(body, Object.class);
        } catch (JacksonException e) {
            return String.format(NOT_JSON, assertion.getPath(), firstLine(e));
        }
        if (value instanceof Map<?, ?> || value instanceof List<?>) {
            return query(value);
        }
        // a body that is only a number, string, boolean or null has nothing but the root to read
        found = "$".equals(assertion.getPath().strip());
        return null;
    }

    private String query(final Object parsed) {
        try {
            value = path.read(parsed, Configuration.defaultConfiguration());
            found = path.isDefinite() || !(value instanceof List<?> list && list.isEmpty());
        } catch (PathNotFoundException e) {
            found = false;
        } catch (RuntimeException e) {
            return String.format(CANNOT_READ_PATH, assertion.getPath(), firstLine(e));
        }
        return null;
    }

    private List<String> existence() {
        boolean wantsAbsent = Boolean.FALSE.equals(assertion.isExists());
        if (found && wantsAbsent) {
            return List.of(String.format(EXISTS_BUT_SHOULD_NOT, text(value)));
        }
        return !found && !wantsAbsent ? List.of(NOT_FOUND) : List.of();
    }

    private List<String> valueChecks() {
        List<String> failures = new ArrayList<>();
        String actual = text(value);
        if (assertion.getEquals() != null && !assertion.getEquals().equals(actual)) {
            failures.add(String.format(WRONG_VALUE, assertion.getEquals(), actual));
        }
        if (assertion.getMatches() != null) {
            matchFailure(assertion.getMatches(), actual).ifPresent(failures::add);
        }
        return failures;
    }

    private static Optional<String> matchFailure(final String regex, final String actual) {
        try {
            boolean ok = Pattern.compile(regex, Pattern.DOTALL).matcher(actual).matches();
            return ok ? Optional.empty() : Optional.of(String.format(NO_MATCH, actual, regex));
        } catch (PatternSyntaxException e) {
            return Optional.of(String.format(BAD_REGEX, regex));
        }
    }

    private static String text(final Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return Configuration.defaultConfiguration().jsonProvider().toJson(value);
    }

    private static String firstLine(final Exception e) {
        String m = String.valueOf(e.getMessage());
        int i = m.indexOf('\n');
        return i < 0 ? m : m.substring(0, i);
    }
}
