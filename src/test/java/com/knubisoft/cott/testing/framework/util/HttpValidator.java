package com.knubisoft.cott.testing.framework.util;


import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.ComparisonException;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.util.LogMessage.RETHROWN_ERRORS_LOG;
import static java.lang.String.format;
import static java.lang.String.valueOf;

@RequiredArgsConstructor
public final class HttpValidator {

    private static final int LIMIT = 100;

    private final List<String> result = new ArrayList<>();
    private final AbstractInterpreter<?> interpreter;

    public void validateCode(final int expectedCode, final int actualCode) {
        if (expectedCode != actualCode) {
            result.add(format(LogMessage.HTTP_CODE_EXPECTED_BUT_WAS, expectedCode, actualCode));
            interpreter.save(valueOf(actualCode));
        }
    }

    public void validateHeaders(final Map<String, String> expectedHeaders, final Map<String, String> actualHeaderMap) {
        if (expectedHeaders != null) {
            try {
                expectedHeaders.entrySet().forEach(each -> validateHeader(each, actualHeaderMap));
            } catch (RuntimeException e) {
                processActualHeaderResult(expectedHeaders, actualHeaderMap);
            }
        }
    }

    private void validateHeader(final Map.Entry<String, String> entry,
                                final Map<String, String> actualHeaderMap) {
        String value = actualHeaderMap.get(entry.getKey());
        if (!StringUtils.equals(entry.getValue(), value)) {
            throw new ComparisonException("Not equal");
        }
    }

    private void processActualHeaderResult(final Map<String, String> expectedHeaders,
                                           final Map<String, String> actualHeaderMap) {
        String expected = interpreter.toString(expectedHeaders);
        String actual = interpreter.toString(actualHeaderMap);
        result.add(format(LogMessage.HTTP_HEADERS_EXPECTED_BUT_WAS, cut(expected), cut(actual)));
        interpreter.save(actual);
    }

    public void validateBody(final String expectedBody, final String actualBody) {
        if (expectedBody != null) {
            try {
                TreeComparator.compare(expectedBody, actualBody);
            } catch (ComparisonException e) {
                result.add(format(LogMessage.HTTP_BODY_EXPECTED_BUT_WAS, cut(expectedBody), cut(actualBody)));
                interpreter.save(actualBody);
            }
        }
    }

    public void rethrowOnErrors() {
        if (!result.isEmpty()) {
            throw new DefaultFrameworkException(RETHROWN_ERRORS_LOG,
                    String.join(DelimiterConstant.SPACE_WITH_LF, result));
        }
    }

    private String cut(final String s) {
        return StringUtils.abbreviate(s, LIMIT);
    }
}
