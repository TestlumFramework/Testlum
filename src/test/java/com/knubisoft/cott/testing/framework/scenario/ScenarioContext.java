package com.knubisoft.cott.testing.framework.scenario;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.UNABLE_FIND_VALUE_FOR_KEY;

@RequiredArgsConstructor
public class ScenarioContext {

    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);

    private final Map<String, String> contextMap;
    private final Map<String, Boolean> conditionMap = new HashMap<>();
    private final String bodyKeyUUID = UUID.randomUUID().toString();

    public void set(final String key, final String value) {
        contextMap.put(key, value);
    }

    public void setCondition(final String key, final Boolean value) {
        conditionMap.put(key, value);
    }

    public String getBody() {
        return contextMap.get(bodyKeyUUID);
    }

    public void setBody(final String value) {
        set(bodyKeyUUID, value);
    }

    public String get(final String key) {
        String result = contextMap.get(key);
        if (result == null) {
            throw new IllegalArgumentException(
                    String.format(UNABLE_FIND_VALUE_FOR_KEY, key, contextMap));
        }
        return result;
    }

    public Boolean getCondition(final String key) {
        Boolean result = conditionMap.get(key);
        if (Objects.isNull(result)) {
            throw new IllegalArgumentException(
                    String.format(UNABLE_FIND_VALUE_FOR_KEY, key, conditionMap));
        }
        return result;
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
            formatted = formatted.replace(zeroSubsequence, value);
        }
        return formatted;
    }
}
