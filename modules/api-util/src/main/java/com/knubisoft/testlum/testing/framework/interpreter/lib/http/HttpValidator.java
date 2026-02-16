package com.knubisoft.testlum.testing.framework.interpreter.lib.http;

import com.knubisoft.testlum.testing.framework.exception.ComparisonException;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public final class HttpValidator {

    public static final String HTTP_CODE_EXPECTED_BUT_WAS = " Http code should be [%s] but was [%s]";
    public static final String HTTP_HEADERS_EXPECTED_BUT_WAS = " Http headers should be [%s]%n but was [%s]";
    public static final String HTTP_BODY_EXPECTED_BUT_WAS = " Http body should be [%s]%n but was [%s]";

    private final List<String> result = new ArrayList<>();
    private final AbstractInterpreter<?> interpreter;

    public void validateCode(final int expectedCode, final int actualCode) {
        if (expectedCode != actualCode) {
            result.add(format(HTTP_CODE_EXPECTED_BUT_WAS, expectedCode, actualCode));
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
        if (!Strings.CS.equals(entry.getValue(), value)) {
            throw new ComparisonException("Not equal");
        }
    }

    private void processActualHeaderResult(final Map<String, String> expectedHeaders,
                                           final Map<String, String> actualHeaderMap) {
        String expected = interpreter.toString(expectedHeaders);
        String actual = interpreter.toString(actualHeaderMap);
        result.add(format(HTTP_HEADERS_EXPECTED_BUT_WAS,
                StringPrettifier.cut(expected), StringPrettifier.cut(actual)));
        interpreter.save(actual);
    }

    public void validateBody(final String expectedBody, final String actualBody) {
        try {
            interpreter.newCompare()
                    .withExpected(expectedBody)
                    .withActual(actualBody)
                    .exec();
        } catch (ComparisonException e) {
            result.add(format(HTTP_BODY_EXPECTED_BUT_WAS,
                    StringPrettifier.asJsonResult(StringPrettifier.cut(expectedBody)),
                    StringPrettifier.asJsonResult(StringPrettifier.cut(actualBody))));
        }
    }

    public void validateBody(final String expectedBody, final String actualBody, final String mode) {
        try {
            interpreter.newCompare()
                    .withExpected(expectedBody)
                    .withActual(actualBody)
                    .withMode(mode)
                    .exec();
        } catch (ComparisonException e) {
            result.add(format(HTTP_BODY_EXPECTED_BUT_WAS,
                    StringPrettifier.asJsonResult(StringPrettifier.cut(expectedBody)),
                    StringPrettifier.asJsonResult(StringPrettifier.cut(actualBody))));
        }
    }

    public void rethrowOnErrors() {
        if (!result.isEmpty()) {
            throw new DefaultFrameworkException(result);
        }
    }
}
