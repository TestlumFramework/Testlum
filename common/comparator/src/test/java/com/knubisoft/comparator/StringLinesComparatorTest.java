package com.knubisoft.comparator;

import com.knubisoft.comparator.exception.MatchException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Unit tests for {@link StringLinesComparator} verifying multi-line string comparison with pattern support. */
class StringLinesComparatorTest {

    private final StringLinesComparator strict = new StringLinesComparator(Mode.STRICT);
    private final StringLinesComparator lenient = new StringLinesComparator(Mode.LENIENT);

    @Test
    void identicalSingleLineDoesNotThrow() {
        assertDoesNotThrow(() -> strict.compare("hello", "hello"));
    }

    @Test
    void identicalMultiLineDoesNotThrow() {
        assertDoesNotThrow(() -> strict.compare("line1\nline2\nline3", "line1\nline2\nline3"));
    }

    @Test
    void differentLineCountThrows() {
        assertThrows(MatchException.class, () -> strict.compare("line1\nline2", "line1"));
    }

    @Test
    void differentContentThrows() {
        assertThrows(MatchException.class, () -> strict.compare("line1\nline2", "line1\nline3"));
    }

    @Test
    void patternMatchingPerLine() {
        assertDoesNotThrow(() -> strict.compare("p(digit)\np(email)", "42\ntest@example.com"));
    }

    @Test
    void patternMismatchPerLineThrows() {
        assertThrows(MatchException.class, () -> strict.compare("p(digit)\np(digit)", "42\nabc"));
    }

    @Test
    void emptyStringsMatch() {
        assertDoesNotThrow(() -> strict.compare("", ""));
    }

    @Test
    void lenientModeWorksWithPatterns() {
        assertDoesNotThrow(() -> lenient.compare("p(any)\nline2", "anything\nline2"));
    }
}
