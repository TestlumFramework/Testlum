package com.knubisoft.testlum.testing.framework.scenario;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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

    private final Map<String, String> contextMap;
    private final Map<String, Boolean> conditionMap = new HashMap<>();

    public ScenarioContext(final Map<String, String> contextMap) {
        this.contextMap = new LinkedHashMap<>(contextMap);
    }

    public Map.Entry<String, String> getBody() {
        Map.Entry<String, String> lastEntryFromLinkedHashMap = getLastEntryFromLinkedHashMap(contextMap);
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
        contextMap.put(key, value);
    }

    public String get(final String key) {
        String result = contextMap.get(key);
        if (result == null) {
            result = String.valueOf(conditionMap.get(key));
            if (result == null || "null".equals(result)) {
                throw new IllegalArgumentException(String.format(NO_VALUE_FOUND_FOR_KEY, key, contextMap));
            }
        }
        return result;
    }

    public void setCondition(final String key, final Boolean value) {
        conditionMap.put(key, value);
    }

    public String getCondition(final String condition) {
        String injectedCondition = condition;
        for (Map.Entry<String, Boolean> entry : conditionMap.entrySet()) {
            if (injectedCondition.contains(entry.getKey())) {
                injectedCondition = injectedCondition.replace(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return injectedCondition;
    }

    public String inject(final String original) {
        if (StringUtils.isBlank(original)) {
            return original;
        }
        Matcher m = ROUTE_PATTERN.matcher(original);
        return getFormattedInject(original, m);
    }

    private String getFormattedInject(final String original, final Matcher m) {
        String formatted = original;
        while (m.find()) {
            String firstSubsequence = m.group(1);
            String zeroSubsequence = m.group(0);
            String value = get(firstSubsequence);
            value = StringEscapeUtils.escapeJson(value);
            formatted = formatted.replace(zeroSubsequence, value);
        }
        return formatted;
    }
}