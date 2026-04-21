package com.knubisoft.testlum.testing.framework.interpreter.lib.http;

import com.knubisoft.testlum.testing.framework.exception.ComparisonException;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HttpValidator {

    public static final String HTTP_CODE_EXPECTED_BUT_WAS = " Http code should be [%s] but was [%s]";
    public static final String HTTP_HEADERS_EXPECTED_BUT_WAS = " Http headers should be [%s]%n but was [%s]";
    public static final String HTTP_BODY_EXPECTED_BUT_WAS = " Http body should be [%s]%n but was [%s]";

    private final List<String> result = new ArrayList<>();
    private final AbstractInterpreter<?> interpreter;
    private StringPrettifier prettifier;
    private String expectedFileName;

    public HttpValidator(AbstractInterpreter<?> interpreter, StringPrettifier stringPrettifier, String expectedFileName) {
        this.interpreter = interpreter;
        this.prettifier = stringPrettifier;
        this.expectedFileName = expectedFileName;
    }

    public HttpValidator(AbstractInterpreter<?> interpreter, StringPrettifier stringPrettifier) {
        this.interpreter = interpreter;
        this.prettifier = stringPrettifier;
    }

    public void validateCode(final int expectedCode, final int actualCode) {
        if (expectedCode != actualCode) {
            result.add(String.format(HTTP_CODE_EXPECTED_BUT_WAS, expectedCode, actualCode));
            interpreter.save(String.valueOf(actualCode), expectedFileName);
        }
    }

    public void validateHeaders(final Map<String, String> expectedHeaders, final Map<String, String> actualHeaderMap) {
        if (Objects.nonNull(expectedHeaders)) {
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
        result.add(String.format(HTTP_HEADERS_EXPECTED_BUT_WAS, prettifier.cut(expected), prettifier.cut(actual)));
        interpreter.save(actual, this.expectedFileName);
    }

    public void validateBody(final String expectedBody, final String actualBody) {
        validateBody(expectedBody, actualBody, true);
    }

    public void validateBody(final String expectedBody, final String actualBody, final boolean isStrict) {
        try {
            interpreter.newCompare()
                    .withExpected(expectedBody)
                    .withActual(actualBody)
                    .withMode(isStrict)
                    .withExpectedFileName(expectedFileName)
                    .exec();
        } catch (ComparisonException e) {
            result.add(String.format(HTTP_BODY_EXPECTED_BUT_WAS,
                    prettifier.asJsonResult(prettifier.cut(expectedBody)),
                    prettifier.asJsonResult(prettifier.cut(actualBody))));
        }
    }

    public void rethrowOnErrors() {
        if (!result.isEmpty()) {
            throw new DefaultFrameworkException(result);
        }
    }
}
