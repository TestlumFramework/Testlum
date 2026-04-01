package com.knubisoft.testlum.testing.framework.exception;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link StopSignalException}. */
class StopSignalExceptionTest {

    @Nested
    class DefaultConstructor {
        @Test
        void messageIsNull() {
            StopSignalException ex = new StopSignalException();
            assertNull(ex.getMessage());
        }

        @Test
        void causeIsNull() {
            StopSignalException ex = new StopSignalException();
            assertNull(ex.getCause());
        }

        @Test
        void canBeInstantiated() {
            assertDoesNotThrow(StopSignalException::new);
        }
    }

    @Nested
    class InheritanceHierarchy {
        @Test
        void isRuntimeException() {
            StopSignalException ex = new StopSignalException();
            assertInstanceOf(RuntimeException.class, ex);
        }

        @Test
        void canBeCaughtAsRuntimeException() {
            assertThrows(RuntimeException.class, () -> {
                throw new StopSignalException();
            });
        }

        @Test
        void canBeThrown() {
            assertThrows(StopSignalException.class, () -> {
                throw new StopSignalException();
            });
        }
    }

    @Nested
    class ClassModifiers {
        @Test
        void isFinalClass() {
            assertTrue(java.lang.reflect.Modifier.isFinal(StopSignalException.class.getModifiers()));
        }
    }
}
