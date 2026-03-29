package com.knubisoft.comparator.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MatchExceptionTest {

    @Test
    void constructedWithMessage() {
        final MatchException ex = new MatchException("test message");
        assertEquals("test message", ex.getMessage());
    }

    @Test
    void isRuntimeException() {
        final MatchException ex = new MatchException("msg");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
