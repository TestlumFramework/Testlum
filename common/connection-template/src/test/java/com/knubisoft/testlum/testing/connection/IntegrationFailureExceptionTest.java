package com.knubisoft.testlum.testing.connection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationFailureExceptionTest {

    @Test
    void storesMessage() {
        IntegrationFailureException exception = new IntegrationFailureException("connection failed");
        assertEquals("connection failed", exception.getMessage());
    }

    @Test
    void extendsRuntimeException() {
        IntegrationFailureException exception = new IntegrationFailureException("test");
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void canBeThrownAndCaught() {
        assertThrows(IntegrationFailureException.class, () -> {
            throw new IntegrationFailureException("test error");
        });
    }

    @Test
    void handlesNullMessage() {
        IntegrationFailureException exception = new IntegrationFailureException(null);
        assertNull(exception.getMessage());
    }

    @Test
    void handlesEmptyMessage() {
        IntegrationFailureException exception = new IntegrationFailureException("");
        assertEquals("", exception.getMessage());
    }
}
