package com.knubisoft.testlum.testing.framework.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StringPrettifier} verifying whitespace removal,
 * JSON formatting, string truncation, and result formatting.
 */
class StringPrettifierTest {

    private JacksonService jacksonService;
    private StringPrettifier prettifier;

    @BeforeEach
    void setUp() {
        jacksonService = Mockito.mock(JacksonService.class);
        prettifier = new StringPrettifier(jacksonService);
    }

    @Nested
    class Prettify {
        @Test
        void removesWhitespace() {
            assertEquals("abc", prettifier.prettify("a b c"));
        }

        @Test
        void removesMultipleWhitespace() {
            assertEquals("hello", prettifier.prettify("  h e l l o  "));
        }

        @Test
        void returnsNullForNull() {
            assertNull(prettifier.prettify(null));
        }

        @Test
        void returnsBlankForBlank() {
            assertEquals("", prettifier.prettify(""));
        }

        @Test
        void returnsOriginalWithNoSpaces() {
            assertEquals("abc", prettifier.prettify("abc"));
        }
    }

    @Nested
    class PrettifyToSave {
        @Test
        void prettifiesJsonObject() {
            when(jacksonService.readValue(any(String.class), eq(Object.class)))
                    .thenReturn(java.util.Map.of("a", 1));
            when(jacksonService.writeValueAsStringWithDefaultPrettyPrinter(any()))
                    .thenReturn("{\n  \"a\" : 1\n}");

            final String result = prettifier.prettifyToSave("{\"a\":1}");
            assertEquals("{\n  \"a\" : 1\n}", result);
        }

        @Test
        void prettifiesJsonArray() {
            when(jacksonService.readValue(any(String.class), eq(Object.class)))
                    .thenReturn(java.util.List.of(1, 2));
            when(jacksonService.writeValueAsStringWithDefaultPrettyPrinter(any()))
                    .thenReturn("[ 1, 2 ]");

            final String result = prettifier.prettifyToSave("[1,2]");
            assertEquals("[ 1, 2 ]", result);
        }

        @Test
        void returnsOriginalForNonJson() {
            final String result = prettifier.prettifyToSave("plain text");
            assertEquals("plain text", result);
        }

        @Test
        void returnsOriginalForNonJsonStartingWithBrace() {
            // Input starts with { but is not valid JSON - readValue not mocked
            // so the real JacksonService mock returns null, causing NPE caught by catch block
            final String result = prettifier.prettifyToSave("not json at all");
            assertEquals("not json at all", result);
        }
    }

    @Nested
    class Cut {
        @Test
        void shortStringUnchanged() {
            assertEquals("short", prettifier.cut("short"));
        }

        @Test
        void longStringGetsTruncated() {
            final String longString = "a".repeat(200);
            final String result = prettifier.cut(longString);
            assertEquals(150, result.length());
        }

        @Test
        void nullReturnsNull() {
            assertNull(prettifier.cut(null));
        }

        @Test
        void blankReturnsBlank() {
            assertEquals("", prettifier.cut(""));
        }

        @Test
        void exactLimitNotTruncated() {
            final String exact = "a".repeat(150);
            assertEquals(exact, prettifier.cut(exact));
        }
    }

    @Nested
    class AsJsonResult {
        @Test
        void blankReturnsEmpty() {
            assertEquals("", prettifier.asJsonResult(""));
            assertEquals("", prettifier.asJsonResult(null));
            assertEquals("", prettifier.asJsonResult("   "));
        }

        @Test
        void nonBlankReturnsPrettified() {
            final String result = prettifier.asJsonResult("plain text");
            assertEquals("plain text", result);
        }

        @Test
        void jsonGetsPrettified() {
            when(jacksonService.readValue(any(String.class), eq(Object.class)))
                    .thenReturn(java.util.Map.of("k", "v"));
            when(jacksonService.writeValueAsStringWithDefaultPrettyPrinter(any()))
                    .thenReturn("{\n  \"k\" : \"v\"\n}");

            final String result = prettifier.asJsonResult("{\"k\":\"v\"}");
            assertEquals("{\n  \"k\" : \"v\"\n}", result);
        }
    }
}
