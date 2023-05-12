package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.OPEN_BRACE;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.OPEN_SQUARE_BRACKET;

@UtilityClass
public class StringPrettifier {

    private static final int CHAR_LIMIT_FOR_CUT = 150;

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
        if (Objects.nonNull(actual) && actual.length() > CHAR_LIMIT_FOR_CUT) {
            return StringUtils.abbreviate(actual, CHAR_LIMIT_FOR_CUT);
        }
        return actual;
    }
}
