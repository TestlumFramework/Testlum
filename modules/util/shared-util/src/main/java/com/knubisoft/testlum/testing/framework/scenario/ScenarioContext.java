package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.util.MapUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

public class ScenarioContext {

    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final String NO_VALUE_FOUND_FOR_KEY = "Unable to find value for key <%s>. Available keys: %s";
    private static final String NO_VALUES_FOUND_IN_CONTEXT = "Unable to find any value in scenario context." +
                                                             " Available keys: %s";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);
    private static final Pattern IDENT = Pattern.compile("\\b([A-Za-z_][A-Za-z0-9_]*)\\b");

    private final Map<String, String> contextMap;
    private final Map<String, Boolean> conditionMap = new HashMap<>();

    public ScenarioContext(final Map<String, String> contextMap) {
        this.contextMap = new LinkedHashMap<>(contextMap);
    }

    public Map.Entry<String, String> getBody() {
        Map.Entry<String, String> lastEntryFromLinkedHashMap = MapUtil.getLastEntryFromLinkedHashMap(contextMap);
        if (lastEntryFromLinkedHashMap == null) {
            throw new IllegalArgumentException(String.format(NO_VALUES_FOUND_IN_CONTEXT, contextMap));
        }
        return lastEntryFromLinkedHashMap;
    }

    public void set(final String key, final String value) {
        contextMap.put(key, value);
    }

    public String get(final String key) {
        String result = contextMap.get(key);
        //must be isNull
        if (isNull(result)) {
            throw new IllegalArgumentException(String.format(NO_VALUE_FOUND_FOR_KEY, key, contextMap));
        }
        return result;
    }

    public void setCondition(final String key, final Boolean value) {
        conditionMap.put(key, value);
    }

    public String getCondition(final String raw) {
        if (StringUtils.isBlank(raw)) {
            return raw;
        }

        Boolean exact = conditionMap.get(raw.trim());
        if (exact != null) {
            return Boolean.toString(exact);
        }
        return substituteIdentifiers(raw, conditionMap);
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
            value = StringEscapeUtils.escapeJson(value).replaceAll("'", "''");
            formatted = formatted.replace(zeroSubsequence, value);
        }
        return formatted;
    }

    private static String substituteIdentifiers(final String expression, final Map<String, Boolean> values) {
        return IDENT.matcher(expression).replaceAll(matchResult -> {
            String ident = matchResult.group(1);
            String replacement = values.containsKey(ident)
                    ? String.valueOf(values.get(ident))
                    : ident;
            return Matcher.quoteReplacement(replacement);
        });
    }
}