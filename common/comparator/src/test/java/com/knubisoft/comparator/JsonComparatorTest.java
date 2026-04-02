package com.knubisoft.comparator;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.knubisoft.comparator.exception.MatchException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
}
