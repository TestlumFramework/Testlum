package com.knubisoft.testlum.testing.framework.util;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.*;

@RequiredArgsConstructor
@Service
public class StringPrettifier {

    private static final int CHAR_LIMIT_FOR_CUT = 150;

    private final JacksonService jacksonService;

    public String prettify(final String string) {
        if (StringUtils.isNotBlank(string)) {
            return string.replaceAll("\\s+", EMPTY);
        }
        return string;
    }

    public String prettifyToSave(final String actual) {
        try {
            return tryToPrettify(actual);
        } catch (Exception ignore) {
            return actual;
        }
    }

    private String tryToPrettify(final String actual) {
        if (actual.startsWith(OPEN_BRACE) || actual.startsWith(OPEN_SQUARE_BRACKET)) {
            Object json = jacksonService.readValue(actual, Object.class);
            return jacksonService.writeValueAsStringWithDefaultPrettyPrinter(json);
        }
        return actual;
    }

    public String cut(final String actual) {
        if (StringUtils.isNotBlank(actual) && actual.length() > CHAR_LIMIT_FOR_CUT) {
            return StringUtils.abbreviate(actual, CHAR_LIMIT_FOR_CUT);
        }
        return actual;
    }

    public String asJsonResult(final String json) {
        return StringUtils.isBlank(json) ? EMPTY : prettifyToSave(json);
    }
}
