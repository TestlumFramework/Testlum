package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class StringPrettifier {

    private static final int CHAR_LIMIT_FOR_CUT = 500;

    private final JacksonService jacksonService;

    public String prettify(final String string) {
        if (StringUtils.isNotBlank(string)) {
            return string.replaceAll("\\s+", DelimiterConstant.EMPTY);
        }
        return string;
    }

    public String prettifyToSave(final String actual) {
        try {
            return tryToPrettify(actual);
        } catch (Exception e) {
            log.debug("Failed to prettify string, returning original value: {}", e.getMessage());
            return actual;
        }
    }

    private String tryToPrettify(final String actual) {
        if (actual.startsWith(DelimiterConstant.OPEN_BRACE)
                || actual.startsWith(DelimiterConstant.OPEN_SQUARE_BRACKET)) {
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
        return StringUtils.isBlank(json) ? DelimiterConstant.EMPTY : prettifyToSave(json);
    }
}
