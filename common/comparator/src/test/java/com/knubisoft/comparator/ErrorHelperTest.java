package com.knubisoft.comparator;

import com.knubisoft.comparator.exception.MatchException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link ErrorHelper} verifying exception throwing behavior for raise methods. */
class ErrorHelperTest {

    @Test
    void raiseWithMessageThrowsMatchException() {
        MatchException ex = assertThrows(MatchException.class, () -> ErrorHelper.raise("error msg"));
        assertEquals("error msg", ex.getMessage());
    }

    @Test
    void raiseWithTrueFlagThrowsMatchException() {
        MatchException ex = assertThrows(MatchException.class, () -> ErrorHelper.raise(true, "flag error"));
        assertEquals("flag error", ex.getMessage());
    }

    @Test
    void raiseWithFalseFlagDoesNotThrow() {
        assertDoesNotThrow(() -> ErrorHelper.raise(false, "should not throw"));
    }
}
