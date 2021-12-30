package com.knubisoft.e2e.testing.framework.scenario;

import com.knubisoft.e2e.testing.framework.util.LogMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ScenarioContext {

    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);

    private final Map<String, String> contextMap;
    private final String bodyKeyUUID = UUID.randomUUID().toString();

    public void set(final String key, final String value) {
        contextMap.put(key, value);
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
                    String.format(LogMessage.UNABLE_FIND_VALUE_FOR_KEY, key, contextMap));
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
