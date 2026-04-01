package com.knubisoft.testlum.testing.framework.exception;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link ComparisonException}. */
class ComparisonExceptionTest {

    @Nested
    class Constructor {
        @Test
        void messageIsPreserved() {
            ComparisonException ex = new ComparisonException("expected X but got Y");
            assertEquals("expected X but got Y", ex.getMessage());
        }

        @Test
        void nullMessageIsAllowed() {
            ComparisonException ex = new ComparisonException(null);
            assertNull(ex.getMessage());
        }

        @Test
        void emptyMessageIsAllowed() {
            ComparisonException ex = new ComparisonException("");
            assertEquals("", ex.getMessage());
        }
    }

    @Nested
    class InheritanceHierarchy {
        @Test
        void isRuntimeException() {
            ComparisonException ex = new ComparisonException("test");
            assertInstanceOf(RuntimeException.class, ex);
        }

        @Test
        void canBeCaughtAsRuntimeException() {
            assertThrows(RuntimeException.class, () -> {
                throw new ComparisonException("fail");
            });
        }
    }

    @Nested
    class CauseChain {
        @Test
        void causeIsNullByDefault() {
            ComparisonException ex = new ComparisonException("msg");
            assertNull(ex.getCause());
        }
    }
}
