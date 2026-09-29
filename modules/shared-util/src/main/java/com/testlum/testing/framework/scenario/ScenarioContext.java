package com.testlum.testing.framework.scenario;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
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

    private final Map<String, Supplier<String>> contextMap;
    private final Map<String, Boolean> conditionMap = new HashMap<>();

    public ScenarioContext(final Map<String, String> contextMap) {
        this.contextMap = new LinkedHashMap<>();
        contextMap.forEach((key, value) -> this.contextMap.put(key, () -> value));
    }

    public Map.Entry<String, Supplier<String>> getBody() {
        Map.Entry<String, Supplier<String>> lastEntryFromLinkedHashMap = getLastEntryFromLinkedHashMap(contextMap);
        if (lastEntryFromLinkedHashMap == null) {
            throw new IllegalArgumentException(String.format(NO_VALUES_FOUND_IN_CONTEXT, contextMap));
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
        contextMap.put(key, () -> value);
    }

    public void setLazy(final String key, final Supplier<String> supplier) {
        contextMap.put(key, Lazy.of(supplier));
    }

    public void setLazyRefreshing(final String key, final Supplier<String> supplier) {
        contextMap.put(key, supplier);
    }

    public boolean containsKey(final String key) {
        return contextMap.containsKey(key) || conditionMap.containsKey(key);
    }

    public String get(final String key) {
        Supplier<String> supplier = contextMap.get(key);
        if (supplier == null) {
            String result = String.valueOf(conditionMap.get(key));
            if ("null".equals(result)) {
                throw new IllegalArgumentException(String.format(NO_VALUE_FOUND_FOR_KEY, key, contextMap));
            }
            return result;
        }
        return supplier.get();
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