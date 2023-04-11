package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.OPEN_BRACE;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.OPEN_SQUARE_BRACKET;

@UtilityClass
public class StringPrettifier {

    private static final int LIMIT = 100;

    public String prettify(final String string) {
        return string.replaceAll("\\s+", DelimiterConstant.EMPTY);
    }

    public static String prettifyToSave(final String actual) {
        try {
            return tryToPrettify(actual);
        } catch (Exception ignore) {
            return actual;
        }
    }

    private static String tryToPrettify(final String actual) {
        if (actual.startsWith(OPEN_BRACE) || actual.startsWith(OPEN_SQUARE_BRACKET)) {
            Object json = JacksonMapperUtil.readValue(actual, Object.class);
            return JacksonMapperUtil.writeValueAsStringWithDefaultPrettyPrinter(json);
        }
        return actual;
    }

    public String cut(final String actual) {
        if (actual.length() > LIMIT) {
            return StringUtils.abbreviate(actual, LIMIT);
        } else {
            return actual;
        }
    }
}
