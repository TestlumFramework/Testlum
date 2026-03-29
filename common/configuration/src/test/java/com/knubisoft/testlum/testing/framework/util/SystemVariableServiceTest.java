package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemVariableServiceTest {

    private final SystemVariableService service = new SystemVariableService();

    @Nested
    class BlankInput {
        @Test
        void nullReturnsNull() {
            assertNull(service.inject(null));
        }

        @Test
        void emptyReturnsEmpty() {
            assertEquals("", service.inject(""));
        }

        @Test
        void blankReturnsBlank() {
            assertEquals("   ", service.inject("   "));
        }
    }

    @Nested
    class NoPlaceholders {
        @Test
        void plainStringReturnedUnchanged() {
            assertEquals("hello world", service.inject("hello world"));
        }

        @Test
        void stringWithDollarButNoPlaceholder() {
            assertEquals("price is $100", service.inject("price is $100"));
        }
    }

    @Nested
    class WithPlaceholders {
        @Test
        void existingEnvVarIsReplaced() {
            final String homeValue = System.getenv("HOME");
            if (homeValue != null) {
                final String result = service.inject("home=${HOME}");
                // inject uses escapeJson which escapes forward slashes
                final String escaped = org.apache.commons.text.StringEscapeUtils.escapeJson(homeValue);
                assertEquals("home=" + escaped, result);
            }
        }

        @Test
        void missingEnvVarThrows() {
            assertThrows(DefaultFrameworkException.class,
                    () -> service.inject("${THIS_VAR_SHOULD_NOT_EXIST_12345}"));
        }

        @Test
        void multiplePlaceholdersWithMissingVarThrows() {
            assertThrows(DefaultFrameworkException.class,
                    () -> service.inject("a=${NONEXISTENT_VAR_A}&b=${NONEXISTENT_VAR_B}"));
        }
    }
}
