package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.ComparisonException;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public final class HttpValidator {

    private final List<String> result = new ArrayList<>();
    private final AbstractInterpreter<?> interpreter;

    public void validateCode(final int expectedCode, final int actualCode) {
        if (expectedCode != actualCode) {
            result.add(format(ExceptionMessage.HTTP_CODE_EXPECTED_BUT_WAS, expectedCode, actualCode));
            interpreter.save(valueOf(actualCode));
        }
    }

    public void validateHeaders(final Map<String, String> expectedHeaders, final Map<String, String> actualHeaderMap) {
        if (nonNull(expectedHeaders)) {
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
        result.add(format(ExceptionMessage.HTTP_HEADERS_EXPECTED_BUT_WAS,
                StringPrettifier.cut(expected), StringPrettifier.cut(actual)));
        interpreter.save(actual);
    }

    public void validateBody(final String expectedBody, final String actualBody) {
        if (nonNull(expectedBody)) {
            try {
                final String newActual = StringPrettifier.prettify(actualBody);
                final String newExpected = StringPrettifier.prettify(expectedBody);
                TreeComparator.compare(newExpected, newActual);
            } catch (ComparisonException e) {
                result.add(format(ExceptionMessage.HTTP_BODY_EXPECTED_BUT_WAS,
                        StringPrettifier.cut(PrettifyStringJson.getJSONResult(expectedBody)),
                        StringPrettifier.cut(PrettifyStringJson.getJSONResult(actualBody))));
                interpreter.save(actualBody);
            }
        }
    }

    public void rethrowOnErrors() {
        if (!result.isEmpty()) {
            throw new DefaultFrameworkException(result);
        }
    }
}
