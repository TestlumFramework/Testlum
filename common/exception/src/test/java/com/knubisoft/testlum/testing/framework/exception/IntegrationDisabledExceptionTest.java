package com.knubisoft.testlum.testing.framework.exception;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link IntegrationDisabledException}. */
class IntegrationDisabledExceptionTest {

    @Nested
    class FormattedMessageConstructor {
        @Test
        void formatsWithSingleArg() {
            IntegrationDisabledException ex = new IntegrationDisabledException(
                    "Integration %s is disabled", "Redis");
            assertEquals("Integration Redis is disabled", ex.getMessage());
        }

        @Test
        void formatsWithMultipleArgs() {
            IntegrationDisabledException ex = new IntegrationDisabledException(
                    "%s on %s is disabled", "Elasticsearch", "env-prod");
            assertEquals("Elasticsearch on env-prod is disabled", ex.getMessage());
        }

        @Test
        void noArgsUsesFormatAsIs() {
            IntegrationDisabledException ex = new IntegrationDisabledException("plain text");
            assertEquals("plain text", ex.getMessage());
        }

        @Test
        void formatsWithIntegerArg() {
            IntegrationDisabledException ex = new IntegrationDisabledException(
                    "Port %d is disabled", 8080);
            assertEquals("Port 8080 is disabled", ex.getMessage());
        }
    }

    @Nested
    class InheritanceHierarchy {
        @Test
        void extendsDefaultFrameworkException() {
            IntegrationDisabledException ex = new IntegrationDisabledException("test");
            assertInstanceOf(DefaultFrameworkException.class, ex);
        }

        @Test
        void isRuntimeException() {
            IntegrationDisabledException ex = new IntegrationDisabledException("test");
            assertInstanceOf(RuntimeException.class, ex);
        }

        @Test
        void canBeCaughtAsDefaultFrameworkException() {
            assertThrows(DefaultFrameworkException.class, () -> {
                throw new IntegrationDisabledException("disabled %s", "mongo");
            });
        }
    }

    @Nested
    class ClassModifiers {
        @Test
        void isFinalClass() {
            assertTrue(java.lang.reflect.Modifier.isFinal(IntegrationDisabledException.class.getModifiers()));
        }
    }
}
