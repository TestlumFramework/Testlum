package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ENV_VARIABLE_NOT_FOUND;

@UtilityClass
public class SystemVariableService {
    private static final String ROUTE_REGEXP = "\\$\\{(.*?)}";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);

    public String inject(final String toInject) {
        if (StringUtils.isBlank(toInject)) {
            return toInject;
        }
        Matcher m = ROUTE_PATTERN.matcher(toInject);
        return getFormattedInject(toInject, m);
    }

    private String getFormattedInject(final String toInject, final Matcher m) {
        String formatted = toInject;
        while (m.find()) {
            String firstSubsequence = m.group(1);
            String zeroSubsequence = m.group(0);
            String value = getValue(firstSubsequence);
            value = StringEscapeUtils.escapeJson(value);
            formatted = formatted.replace(zeroSubsequence, value);
        }
        return formatted;
    }

    private String getValue(final String key) {
        Map<String, String> env = System.getenv();
        if (Objects.isNull(env.get(key))) {
            throw new DefaultFrameworkException(ENV_VARIABLE_NOT_FOUND, key);
        }
        return env.get(key);
    }
}
