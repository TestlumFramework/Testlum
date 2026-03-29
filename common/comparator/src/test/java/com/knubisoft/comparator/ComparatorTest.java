package com.knubisoft.comparator;

import com.knubisoft.comparator.exception.MatchException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link Comparator} verifying JSON, XML, and string routing with strict and lenient modes. */
class ComparatorTest {

    @Test
    void strictFactoryCreatesInstance() {
        assertNotNull(Comparator.strict());
    }

    @Test
    void lenientFactoryCreatesInstance() {
        assertNotNull(Comparator.lenient());
    }

    @Nested
    class JsonRouting {
        @Test
        void matchingJsonDoesNotThrow() {
            assertDoesNotThrow(() -> Comparator.strict().compare(
                    "{\"name\":\"John\",\"age\":30}",
                    "{\"name\":\"John\",\"age\":30}"));
        }

        @Test
        void mismatchedJsonThrows() {
            assertThrows(MatchException.class, () -> Comparator.strict().compare(
                    "{\"name\":\"John\"}",
                    "{\"name\":\"Jane\"}"));
        }

        @Test
        void jsonArrayComparison() {
            assertDoesNotThrow(() -> Comparator.strict().compare(
                    "[1,2,3]", "[1,2,3]"));
        }

        @Test
        void lenientJsonIgnoresExtraFields() {
            assertDoesNotThrow(() -> Comparator.lenient().compare(
                    "{\"a\":1}",
                    "{\"a\":1,\"b\":2}"));
        }

        @Test
        void strictJsonFailsOnExtraFields() {
            assertThrows(MatchException.class, () -> Comparator.strict().compare(
                    "{\"a\":1}",
                    "{\"a\":1,\"b\":2}"));
        }
    }

    @Nested
    class XmlRouting {
        @Test
        void matchingXmlDoesNotThrow() {
            assertDoesNotThrow(() -> Comparator.lenient().compare(
                    "<root><child>text</child></root>",
                    "<root><child>text</child></root>"));
        }

        @Test
        void mismatchedXmlThrows() {
            assertThrows(MatchException.class, () -> Comparator.lenient().compare(
                    "<root><child>text1</child></root>",
                    "<root><child>text2</child></root>"));
        }
    }

    @Nested
    class StringRouting {
        @Test
        void matchingStringsDoNotThrow() {
            assertDoesNotThrow(() -> Comparator.strict().compare(
                    "hello world", "hello world"));
        }

        @Test
        void mismatchedStringsThrow() {
            assertThrows(MatchException.class, () -> Comparator.strict().compare(
                    "hello", "world"));
        }

        @Test
        void multiLineStringsMatch() {
            assertDoesNotThrow(() -> Comparator.strict().compare(
                    "line1\nline2", "line1\nline2"));
        }

        @Test
        void multiLineStringsMismatchThrows() {
            assertThrows(MatchException.class, () -> Comparator.strict().compare(
                    "line1\nline2", "line1\nline3"));
        }
    }

    @Nested
    class PatternMatchingViaComparator {
        @Test
        void jsonWithPatterns() {
            final String uuid = "550e8400-e29b-41d4-a716-446655440000";
            assertDoesNotThrow(() -> Comparator.strict().compare(
                    "{\"id\":\"p(uuid)\",\"name\":\"John\"}",
                    "{\"id\":\"" + uuid + "\",\"name\":\"John\"}"));
        }

        @Test
        void plainTextPatternDoesNotRouteToString() {
            // "42" is valid JSON, so StringLinesHandler skips it
            assertThrows(MatchException.class, () -> Comparator.strict().compare(
                    "p(digit)", "42"));
        }

        @Test
        void nonJsonStringPatternWorks() {
            // Both sides are non-JSON, non-XML plain text
            assertDoesNotThrow(() -> Comparator.strict().compare(
                    "p(any)", "hello world"));
        }
    }
}
