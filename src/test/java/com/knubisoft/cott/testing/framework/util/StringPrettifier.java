package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import lombok.experimental.UtilityClass;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.OPEN_BRACE;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.OPEN_SQUARE_BRACKET;

@UtilityClass
public class StringPrettifier {

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
}
