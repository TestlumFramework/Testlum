package com.testlum.testing.framework.scenario;

import com.testlum.testing.framework.util.TokenMasker;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScenarioContext {

    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final String NO_VALUE_FOUND_FOR_KEY =
            "Unable to find value for key <%s>. Available keys: %s";
    private static final String NO_VALUES_FOUND_IN_CONTEXT =
            "Unable to find any value in scenario context. Available keys: %s";
    private static final Pattern ROUTE_PATTERN =
            Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);
    private static final Pattern CONDITION_NAME_PATTERN = Pattern.compile("\\b([A-Za-z_][A-Za-z0-9_]*)\\b");

    private final Map<String, String> contextMap;
    private final Map<String, Boolean> conditionMap = new HashMap<>();
    private final Map<String, String> authVariables = new HashMap<>();
    private final Set<String> secretValues = new LinkedHashSet<>();

    public ScenarioContext(final Map<String, String> contextMap) {
        this.contextMap = new LinkedHashMap<>(contextMap);
    }

    public Map.Entry<String, String> getBody() {
        Map.Entry<String, String> lastEntryFromLinkedHashMap = getLastEntryFromLinkedHashMap(contextMap);
        if (lastEntryFromLinkedHashMap == null) {
            throw new IllegalArgumentException(String.format(NO_VALUES_FOUND_IN_CONTEXT, contextMap.keySet()));
        }
        return lastEntryFromLinkedHashMap;
    }

    private static <K, V> Map.Entry<K, V> getLastEntryFromLinkedHashMap(final Map<K, V> map) {
        Map.Entry<K, V> last = null;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            last = entry;
        }
        return last;
    }

    public void set(final String key, final String value) {
        contextMap.put(key, value);
    }

    public void setVariable(final String key, final String value) {
        if (authVariables.containsKey(key) || containsSecret(value)) {
            authVariables.put(key, value);
        } else {
            set(key, value);
        }
    }

    public void setAuthToken(final String key, final String token) {
        if (StringUtils.isBlank(key) || containsKey(key)) {
            throw new IllegalArgumentException("Auth token variable must have an unused nonblank name");
        }
        if (StringUtils.isBlank(token)) {
            throw new IllegalArgumentException("Auth strategy did not provide an access token");
        }
        registerSecret(token);
        authVariables.put(key, token);
    }

    public void clearAuthVariables() {
        authVariables.clear();
    }

    private boolean containsSecret(final String value) {
        return value != null && secretValues.stream().anyMatch(value::contains);
    }

    private void registerSecret(final String value) {
        if (StringUtils.isNotEmpty(value)) {
            secretValues.add(value);
            secretValues.add(StringEscapeUtils.escapeJson(value));
        }
    }

    public String redact(final String value) {
        if (value == null || secretValues.isEmpty()) {
            return value;
        }
        String redacted = value;
        for (String secret : secretValues.stream().sorted((left, right) -> right.length() - left.length()).toList()) {
            redacted = redacted.replace(secret, TokenMasker.mask(secret));
        }
        return redacted;
    }

    public boolean containsKey(final String key) {
        return authVariables.containsKey(key) || contextMap.containsKey(key) || conditionMap.containsKey(key);
    }

    public String get(final String key) {
        if (authVariables.containsKey(key)) {
            return authVariables.get(key);
        }
        String result = contextMap.get(key);
        if (result == null) {
            result = String.valueOf(conditionMap.get(key));
            if (result == null || "null".equals(result)) {
                throw new IllegalArgumentException(redact(String.format(NO_VALUE_FOUND_FOR_KEY,
                        key, contextMap.keySet())));
            }
        }
        return result;
    }

    public void setCondition(final String key, final Boolean value) {
        conditionMap.put(key, value);
    }

    public String getCondition(final String condition) {
        Boolean exact = conditionMap.get(condition.trim());
        if (exact != null) {
            return Boolean.toString(exact);
        }
        return substituteIdentifiers(condition);
    }

    public String inject(final String original) {
        return inject(original, false);
    }

    private String inject(final String original, final boolean escapeSpelQuotes) {
        if (StringUtils.isBlank(original)) {
            return original;
        }
        Matcher m = ROUTE_PATTERN.matcher(original);
        return getFormattedInject(original, m, escapeSpelQuotes);
    }

    public String injectSpel(final String original) {
        return inject(original, true);
    }

    private String getFormattedInject(final String original, final Matcher m, final boolean escapeSpelQuotes) {
        String formatted = original;
        while (m.find()) {
            String firstSubsequence = m.group(1);
            String zeroSubsequence = m.group(0);
            String value = get(firstSubsequence);
            value = escapeSpelQuotes ? escapeSpelQuotes(value) : StringEscapeUtils.escapeJson(value);
            formatted = formatted.replace(zeroSubsequence, value);
        }
        return formatted;
    }

    private String escapeSpelQuotes(final String value) {
        return value.replaceAll("'", "''");
    }

    private String substituteIdentifiers(final String expression) {
        return CONDITION_NAME_PATTERN.matcher(expression).replaceAll(matchResult -> {
            String conditionName = matchResult.group(1);
            String replacement = conditionMap.containsKey(conditionName)
                    ? String.valueOf(conditionMap.get(conditionName))
                    : conditionName;
            return Matcher.quoteReplacement(replacement);
        });
    }
}
