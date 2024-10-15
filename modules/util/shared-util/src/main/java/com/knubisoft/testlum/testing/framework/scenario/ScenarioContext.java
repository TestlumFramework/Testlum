package com.knubisoft.testlum.testing.framework.scenario;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class ScenarioContext {

    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final String NO_VALUE_FOUND_FOR_KEY = "Unable to find value for key <%s>. Available keys: %s";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);

    private final Map<String, String> contextMap;
    private final Map<String, Boolean> conditionMap = new HashMap<>();

    private final String bodyKeyUUID = UUID.randomUUID().toString();

    public void setBody(final String value) {
        set(bodyKeyUUID, value);
    }

    public String getBody() {
        return contextMap.get(bodyKeyUUID);
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
