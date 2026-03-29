package com.knubisoft.testlum.testing.framework.interpreter.lib.http;

import com.knubisoft.testlum.testing.framework.exception.ComparisonException;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class HttpValidatorTest {

    private AbstractInterpreter<?> interpreter;
    private StringPrettifier prettifier;
    private HttpValidator validator;

    @BeforeEach
    void setUp() {
        interpreter = mock(AbstractInterpreter.class);
        prettifier = mock(StringPrettifier.class);
        validator = new HttpValidator(interpreter, prettifier);
        when(prettifier.cut(anyString())).thenAnswer(inv -> inv.getArgument(0));
        when(prettifier.asJsonResult(anyString())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    class ValidateCode {
        @Test
        void matchingCodesNoError() {
            validator.validateCode(200, 200);
            assertDoesNotThrow(() -> validator.rethrowOnErrors());
        }

        @Test
        void mismatchedCodesAddsError() {
            validator.validateCode(200, 404);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.rethrowOnErrors());
        }

        @Test
        void mismatchedCodeSavesActual() {
            validator.validateCode(200, 500);
            verify(interpreter).save("500");
        }

        @Test
        void matchingCodesDoNotSave() {
            validator.validateCode(200, 200);
            verify(interpreter, never()).save(anyString());
        }

        @Test
        void matching201Codes() {
            validator.validateCode(201, 201);
            assertDoesNotThrow(() -> validator.rethrowOnErrors());
        }

        @Test
        void matching404Codes() {
            validator.validateCode(404, 404);
            assertDoesNotThrow(() -> validator.rethrowOnErrors());
        }

        @Test
        void mismatch201vs200() {
            validator.validateCode(201, 200);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.rethrowOnErrors());
        }
    }

    @Nested
    class ValidateHeaders {
        @Test
        void nullExpectedHeadersNoError() {
            validator.validateHeaders(null, new HashMap<>());
            assertDoesNotThrow(() -> validator.rethrowOnErrors());
        }

        @Test
        void matchingHeadersNoError() {
            final Map<String, String> expected = Map.of("Content-Type", "application/json");
            final Map<String, String> actual = Map.of("Content-Type", "application/json");
            validator.validateHeaders(expected, actual);
            assertDoesNotThrow(() -> validator.rethrowOnErrors());
        }

        @Test
        void mismatchedHeadersAddsError() {
            final Map<String, String> expected = new HashMap<>();
            expected.put("Content-Type", "application/json");
            final Map<String, String> actual = new HashMap<>();
            actual.put("Content-Type", "text/html");
            when(interpreter.toString(any())).thenReturn("{}");
            validator.validateHeaders(expected, actual);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.rethrowOnErrors());
        }

        @Test
        void missingHeaderKeyAddsError() {
            final Map<String, String> expected = new HashMap<>();
            expected.put("X-Custom", "value");
            final Map<String, String> actual = new HashMap<>();
            when(interpreter.toString(any())).thenReturn("{}");
            validator.validateHeaders(expected, actual);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.rethrowOnErrors());
        }

        @Test
        void multipleMatchingHeaders() {
            final Map<String, String> expected = new HashMap<>();
            expected.put("Content-Type", "application/json");
            expected.put("Accept", "text/html");
            final Map<String, String> actual = new HashMap<>();
            actual.put("Content-Type", "application/json");
            actual.put("Accept", "text/html");
            validator.validateHeaders(expected, actual);
            assertDoesNotThrow(() -> validator.rethrowOnErrors());
        }

        @Test
        void emptyExpectedHeadersNoError() {
            final Map<String, String> expected = new HashMap<>();
            final Map<String, String> actual = new HashMap<>();
            actual.put("Content-Type", "text/html");
            validator.validateHeaders(expected, actual);
            assertDoesNotThrow(() -> validator.rethrowOnErrors());
        }

        @Test
        void mismatchedHeaderSavesActual() {
            final Map<String, String> expected = new HashMap<>();
            expected.put("X-Custom", "expected-val");
            final Map<String, String> actual = new HashMap<>();
            actual.put("X-Custom", "actual-val");
            when(interpreter.toString(any())).thenReturn("formatted");
            validator.validateHeaders(expected, actual);
            verify(interpreter).save("formatted");
        }
    }

    @Nested
    class ValidateBody {
        @Test
        void matchingBodyNoError() {
            final CompareBuilder builder = mock(CompareBuilder.class);
            when(interpreter.newCompare()).thenReturn(builder);
            when(builder.withExpected(anyString())).thenReturn(builder);
            when(builder.withActual(anyString())).thenReturn(builder);
            when(builder.withMode(true)).thenReturn(builder);

            validator.validateBody("expected", "actual");
            assertDoesNotThrow(() -> validator.rethrowOnErrors());
        }

        @Test
        void nonStrictModePassesFlag() {
            final CompareBuilder builder = mock(CompareBuilder.class);
            when(interpreter.newCompare()).thenReturn(builder);
            when(builder.withExpected(anyString())).thenReturn(builder);
            when(builder.withActual(anyString())).thenReturn(builder);
            when(builder.withMode(false)).thenReturn(builder);

            validator.validateBody("exp", "act", false);
            verify(builder).withMode(false);
        }

        @Test
        void strictModePassesFlag() {
            final CompareBuilder builder = mock(CompareBuilder.class);
            when(interpreter.newCompare()).thenReturn(builder);
            when(builder.withExpected(anyString())).thenReturn(builder);
            when(builder.withActual(anyString())).thenReturn(builder);
            when(builder.withMode(true)).thenReturn(builder);

            validator.validateBody("exp", "act", true);
            verify(builder).withMode(true);
        }

        @Test
        void defaultValidateBodyUsesStrictMode() {
            final CompareBuilder builder = mock(CompareBuilder.class);
            when(interpreter.newCompare()).thenReturn(builder);
            when(builder.withExpected(anyString())).thenReturn(builder);
            when(builder.withActual(anyString())).thenReturn(builder);
            when(builder.withMode(true)).thenReturn(builder);

            validator.validateBody("expected", "actual");
            verify(builder).withMode(true);
        }

        @Test
        void validateBodyCallsExec() {
            final CompareBuilder builder = mock(CompareBuilder.class);
            when(interpreter.newCompare()).thenReturn(builder);
            when(builder.withExpected(anyString())).thenReturn(builder);
            when(builder.withActual(anyString())).thenReturn(builder);
            when(builder.withMode(true)).thenReturn(builder);

            validator.validateBody("expected", "actual");
            verify(builder).exec();
        }
    }

    @Nested
    class ValidateBodyComparisonFailure {
        @Test
        void comparisonExceptionAddsError() {
            final CompareBuilder builder = mock(CompareBuilder.class);
            when(interpreter.newCompare()).thenReturn(builder);
            when(builder.withExpected(anyString())).thenReturn(builder);
            when(builder.withActual(anyString())).thenReturn(builder);
            when(builder.withMode(true)).thenReturn(builder);
            Mockito.doThrow(new ComparisonException("mismatch"))
                    .when(builder).exec();

            validator.validateBody("expected", "actual");
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.rethrowOnErrors());
        }

        @Test
        void comparisonExceptionInNonStrictModeAddsError() {
            final CompareBuilder builder = mock(CompareBuilder.class);
            when(interpreter.newCompare()).thenReturn(builder);
            when(builder.withExpected(anyString())).thenReturn(builder);
            when(builder.withActual(anyString())).thenReturn(builder);
            when(builder.withMode(false)).thenReturn(builder);
            Mockito.doThrow(new ComparisonException("mismatch"))
                    .when(builder).exec();

            validator.validateBody("expected", "actual", false);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.rethrowOnErrors());
        }
    }

    @Nested
    class RethrowOnErrors {
        @Test
        void noErrorsDoesNotThrow() {
            assertDoesNotThrow(() -> validator.rethrowOnErrors());
        }

        @Test
        void multipleErrorsCollected() {
            validator.validateCode(200, 404);
            validator.validateCode(200, 500);
            assertThrows(DefaultFrameworkException.class,
                    () -> validator.rethrowOnErrors());
        }

        @Test
        void codeAndHeaderErrorsCollected() {
            validator.validateCode(200, 500);

            final Map<String, String> expected = new HashMap<>();
            expected.put("X-Custom", "value");
            final Map<String, String> actual = new HashMap<>();
            when(interpreter.toString(any())).thenReturn("{}");
            validator.validateHeaders(expected, actual);

            assertThrows(DefaultFrameworkException.class,
                    () -> validator.rethrowOnErrors());
        }

        @Test
        void codeAndBodyErrorsCollected() {
            validator.validateCode(200, 500);

            final CompareBuilder builder = mock(CompareBuilder.class);
            when(interpreter.newCompare()).thenReturn(builder);
            when(builder.withExpected(anyString())).thenReturn(builder);
            when(builder.withActual(anyString())).thenReturn(builder);
            when(builder.withMode(true)).thenReturn(builder);
            Mockito.doThrow(new ComparisonException("mismatch"))
                    .when(builder).exec();
            validator.validateBody("expected", "actual");

            assertThrows(DefaultFrameworkException.class,
                    () -> validator.rethrowOnErrors());
        }
    }
}
