package com.testlum.testing.framework.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VaultUnavailableExceptionTest {

    @Test
    void constructorPreservesMessageAndCause() {
        final Throwable cause = new RuntimeException("root cause");
        final VaultUnavailableException ex = new VaultUnavailableException("vault down", cause);

        assertEquals("vault down", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertEquals("vault down", ex.describe());
        assertFalse(ex.isRetryable());
        assertInstanceOf(DefaultFrameworkException.class, ex);
        assertInstanceOf(FormattedFailure.class, ex);
    }
}
