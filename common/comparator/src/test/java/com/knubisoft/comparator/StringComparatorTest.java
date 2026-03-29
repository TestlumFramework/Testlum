package com.knubisoft.comparator;

import com.knubisoft.comparator.exception.MatchException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Unit tests for {@link StringComparator} verifying pattern, tree, and condition comparison modes. */
class StringComparatorTest {

    private final StringComparator strict = new StringComparator(Mode.STRICT);
    private final StringComparator lenient = new StringComparator(Mode.LENIENT);

    @Nested
    class EqualStrings {
        @Test
        void identicalStringsDoNotThrow() {
            assertDoesNotThrow(() -> strict.compare("hello", "hello"));
        }

        @Test
        void bothNullDoNotThrow() {
            assertDoesNotThrow(() -> strict.compare(null, null));
        }

        @Test
        void emptyStringsDoNotThrow() {
            assertDoesNotThrow(() -> strict.compare("", ""));
        }
    }

    @Nested
    class UnequalStrings {
        @Test
        void differentStringsThrowWhenNoPattern() {
            assertThrows(MatchException.class, () -> strict.compare("abc", "xyz"));
        }

        @Test
        void shortExpectedStringThrowsWithEmpty() {
            // length <= 3, so extractAction returns Empty -> raises error
            assertThrows(MatchException.class, () -> strict.compare("ab", "xy"));
        }
    }

    @Nested
    class PatternComparison {
        @Test
        void singleAliasPatternMatches() {
            assertDoesNotThrow(() -> strict.compare("p(digit)", "42"));
        }

        @Test
        void singleAliasPatternDoesNotMatch() {
            assertThrows(MatchException.class, () -> strict.compare("p(digit)", "abc"));
        }

        @Test
        void anyAliasMatchesAnything() {
            assertDoesNotThrow(() -> strict.compare("p(any)", "literally anything"));
        }

        @Test
        void uuidAliasMatches() {
            assertDoesNotThrow(() -> strict.compare("p(uuid)", "550e8400-e29b-41d4-a716-446655440000"));
        }

        @Test
        void emailAliasMatches() {
            assertDoesNotThrow(() -> strict.compare("p(email)", "test@example.com"));
        }

        @Test
        void rawRegexPatternMatches() {
            assertDoesNotThrow(() -> strict.compare("p([A-Z]{3})", "ABC"));
        }

        @Test
        void rawRegexPatternDoesNotMatch() {
            assertThrows(MatchException.class, () -> strict.compare("p([A-Z]{3})", "abc"));
        }

        @Test
        void compositePatternWithAlias() {
            // Mixed expression: text + p(alias) -- uses Parser
            assertDoesNotThrow(() -> strict.compare("ID-p(digit)", "ID-42"));
        }

        @Test
        void compositePatternDoesNotMatch() {
            assertThrows(MatchException.class, () -> strict.compare("ID-p(digit)", "ID-abc"));
        }
    }

    @Nested
    class TreeComparison {
        @Test
        void treeComparisonWithJsonDelegates() {
            assertDoesNotThrow(() -> strict.compare("t({\"a\":1})", "{\"a\":1}"));
        }

        @Test
        void treeComparisonWithMismatchThrows() {
            assertThrows(MatchException.class, () -> strict.compare("t({\"a\":1})", "{\"a\":2}"));
        }

        @Test
        void treeComparisonWithStringDelegates() {
            assertDoesNotThrow(() -> strict.compare("t(hello)", "hello"));
        }
    }

    @Nested
    class ConditionComparison {
        @Test
        void integerConditionMoreThan() {
            assertDoesNotThrow(() -> strict.compare("c(>5)", "10"));
        }

        @Test
        void integerConditionMoreThanFails() {
            assertThrows(MatchException.class, () -> strict.compare("c(>5)", "3"));
        }

        @Test
        void integerConditionLessThan() {
            assertDoesNotThrow(() -> strict.compare("c(<100)", "50"));
        }

        @Test
        void integerConditionMoreThanOrEqual() {
            assertDoesNotThrow(() -> strict.compare("c(>=5)", "5"));
            assertDoesNotThrow(() -> strict.compare("c(>=5)", "10"));
        }

        @Test
        void integerConditionLessThanOrEqual() {
            assertDoesNotThrow(() -> strict.compare("c(<=10)", "10"));
            assertDoesNotThrow(() -> strict.compare("c(<=10)", "5"));
        }

        @Test
        void decimalCondition() {
            assertDoesNotThrow(() -> strict.compare("c(>5.5)", "10.5"));
            assertThrows(MatchException.class, () -> strict.compare("c(>5.5)", "3.0"));
        }

        @Test
        void dateCondition() {
            assertDoesNotThrow(() -> strict.compare("c(>2024-01-01)", "2024-06-15"));
            assertThrows(MatchException.class, () -> strict.compare("c(>2024-06-15)", "2024-01-01"));
        }
    }

    @Nested
    class LenientMode {
        @Test
        void patternWorksInLenientMode() {
            assertDoesNotThrow(() -> lenient.compare("p(digit)", "42"));
        }

        @Test
        void conditionWorksInLenientMode() {
            assertDoesNotThrow(() -> lenient.compare("c(>5)", "10"));
        }
    }
}
