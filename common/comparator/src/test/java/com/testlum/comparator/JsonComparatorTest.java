package com.testlum.comparator;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.testlum.comparator.exception.MatchException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Unit tests for {@link JsonComparator} verifying boolean, number, string, array, and object comparison. */
class JsonComparatorTest {

    private static final JsonMapper MAPPER = new JsonMapper();

    private final JsonComparator strict = new JsonComparator(Mode.STRICT);
    private final JsonComparator lenient = new JsonComparator(Mode.LENIENT);

    private JsonNode node(final String json) {
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class BooleanComparison {
        @Test
        void equalBooleansDoNotThrow() {
            assertDoesNotThrow(() -> strict.compare(node("true"), node("true")));
            assertDoesNotThrow(() -> strict.compare(node("false"), node("false")));
        }

        @Test
        void differentBooleansThrow() {
            assertThrows(MatchException.class,
                    () -> strict.compare(node("true"), node("false")));
        }
    }

    @Nested
    class NumberComparison {
        @Test
        void equalNumbersDoNotThrow() {
            assertDoesNotThrow(() -> strict.compare(node("42"), node("42")));
            assertDoesNotThrow(() -> strict.compare(node("3.14"), node("3.14")));
        }

        @Test
        void differentNumbersThrow() {
            assertThrows(MatchException.class,
                    () -> strict.compare(node("42"), node("43")));
        }
    }

    @Nested
    class StringNodeComparison {
        @Test
        void equalStringsDoNotThrow() {
            assertDoesNotThrow(
                    () -> strict.compare(node("\"hello\""), node("\"hello\"")));
        }

        @Test
        void differentStringsThrow() {
            assertThrows(MatchException.class,
                    () -> strict.compare(node("\"hello\""), node("\"world\"")));
        }

        @Test
        void stringPatternMatchingWorks() {
            assertDoesNotThrow(
                    () -> strict.compare(node("\"p(digit)\""), node("\"42\"")));
        }

        @Test
        void stringToNumberComparison() {
            assertDoesNotThrow(() -> strict.compare(node("\"42\""), node("42")));
        }

        @Test
        void stringToBooleanComparison() {
            assertDoesNotThrow(
                    () -> strict.compare(node("\"true\""), node("true")));
        }
    }

    @Nested
    class ArrayComparison {
        @Test
        void equalArraysDoNotThrow() {
            assertDoesNotThrow(
                    () -> strict.compare(node("[1,2,3]"), node("[1,2,3]")));
        }

        @Test
        void differentArrayLengthThrows() {
            assertThrows(MatchException.class,
                    () -> strict.compare(node("[1,2]"), node("[1,2,3]")));
        }

        @Test
        void differentArrayElementThrows() {
            assertThrows(MatchException.class,
                    () -> strict.compare(node("[1,2,3]"), node("[1,2,4]")));
        }

        @Test
        void emptyArraysDoNotThrow() {
            assertDoesNotThrow(() -> strict.compare(node("[]"), node("[]")));
        }

        @Test
        void nestedArraysDoNotThrow() {
            assertDoesNotThrow(
                    () -> strict.compare(node("[[1,2],[3]]"), node("[[1,2],[3]]")));
        }
    }

    @Nested
    class ObjectComparison {
        @Test
        void equalObjectsDoNotThrow() {
            assertDoesNotThrow(() -> strict.compare(
                    node("{\"a\":1,\"b\":2}"),
                    node("{\"a\":1,\"b\":2}")));
        }

        @Test
        void missingPropertyThrows() {
            assertThrows(MatchException.class, () -> strict.compare(
                    node("{\"a\":1,\"b\":2}"),
                    node("{\"a\":1}")));
        }

        @Test
        void differentPropertyValueThrows() {
            assertThrows(MatchException.class, () -> strict.compare(
                    node("{\"a\":1}"),
                    node("{\"a\":2}")));
        }

        @Test
        void strictModeExtraPropertiesThrows() {
            assertThrows(MatchException.class, () -> strict.compare(
                    node("{\"a\":1}"),
                    node("{\"a\":1,\"b\":2}")));
        }

        @Test
        void lenientModeExtraPropertiesOk() {
            assertDoesNotThrow(() -> lenient.compare(
                    node("{\"a\":1}"),
                    node("{\"a\":1,\"b\":2}")));
        }

        @Test
        void nestedObjectsDoNotThrow() {
            assertDoesNotThrow(() -> strict.compare(
                    node("{\"a\":{\"b\":1}}"),
                    node("{\"a\":{\"b\":1}}")));
        }

        @Test
        void nestedObjectMismatchThrows() {
            assertThrows(MatchException.class, () -> strict.compare(
                    node("{\"a\":{\"b\":1}}"),
                    node("{\"a\":{\"b\":2}}")));
        }
    }

    @Nested
    class TypeMismatch {
        @Test
        void booleanVsArrayThrows() {
            assertThrows(MatchException.class,
                    () -> strict.compare(node("true"), node("[1]")));
        }

        @Test
        void numberVsObjectThrows() {
            assertThrows(MatchException.class,
                    () -> strict.compare(node("42"), node("{\"a\":1}")));
        }

        @Test
        void arrayVsObjectThrows() {
            assertThrows(MatchException.class,
                    () -> strict.compare(node("[1]"), node("{\"a\":1}")));
        }
    }

    @Nested
    class PatternMatchingInJson {
        @Test
        void patternInObjectValue() {
            final String uuid = "550e8400-e29b-41d4-a716-446655440000";
            assertDoesNotThrow(() -> strict.compare(
                    node("{\"id\":\"p(uuid)\"}"),
                    node("{\"id\":\"" + uuid + "\"}")));
        }

        @Test
        void patternInArrayElement() {
            assertDoesNotThrow(() -> strict.compare(
                    node("[\"p(digit)\",\"hello\"]"),
                    node("[\"42\",\"hello\"]")));
        }

        @Test
        void conditionInObjectValue() {
            assertDoesNotThrow(() -> strict.compare(
                    node("{\"count\":\"c(>0)\"}"),
                    node("{\"count\":\"10\"}")));
        }
    }

    @Nested
    class MismatchPaths {
        private String message(final String expected, final String actual) {
            return assertThrows(MatchException.class, () -> strict.compare(node(expected), node(actual))).getMessage();
        }

        @Test
        void nestedFieldNamesItsPath() {
            assertEquals("$.team.lead: Property [Ana] is not equal to [Bob]",
                    message("{\"id\":1,\"team\":{\"lead\":\"Ana\"}}", "{\"id\":1,\"team\":{\"lead\":\"Bob\"}}"));
        }

        @Test
        void arrayElementNamesItsIndex() {
            assertEquals("$.items[1].qty: Property [2] is not equal to [3]",
                    message("{\"items\":[{\"qty\":1},{\"qty\":2}]}", "{\"items\":[{\"qty\":1},{\"qty\":3}]}"));
        }

        @Test
        void topLevelValueIsTheRoot() {
            assertEquals("$: Property [1] is not equal to [2]", message("1", "2"));
        }

        @Test
        void missingPropertyNamesTheParent() {
            assertEquals("$.team: Difference in properties: expected 1 actual 1, missing [lead], unexpected [other]",
                    message("{\"team\":{\"lead\":\"Ana\"}}", "{\"team\":{\"other\":\"x\"}}"));
        }

        @Test
        void strictCountMismatchListsMissingAndUnexpected() {
            assertEquals("$: Difference in properties: expected 2 actual 3, missing [b], unexpected [c, d]",
                    message("{\"a\":1,\"b\":2}", "{\"a\":1,\"c\":3,\"d\":4}"));
        }

        @Test
        void oddKeysUseBrackets() {
            assertEquals("$['first-name']: Property [A] is not equal to [B]",
                    message("{\"first-name\":\"A\"}", "{\"first-name\":\"B\"}"));
        }

        @Test
        void quotesAndBackslashesInAKeyAreEscaped() {
            assertEquals("$['it\\'s']: Property [A] is not equal to [B]",
                    message("{\"it's\":\"A\"}", "{\"it's\":\"B\"}"));
            assertEquals("$['a\\\\b']: Property [A] is not equal to [B]",
                    message("{\"a\\\\b\":\"A\"}", "{\"a\\\\b\":\"B\"}"));
        }

        @Test
        void strictSameSizeWithDifferentKeysStillListsMissingAndUnexpected() {
            assertEquals("$: Difference in properties: expected 2 actual 2, missing [b], unexpected [c]",
                    message("{\"a\":1,\"b\":2}", "{\"a\":1,\"c\":3}"));
        }
    }
}
