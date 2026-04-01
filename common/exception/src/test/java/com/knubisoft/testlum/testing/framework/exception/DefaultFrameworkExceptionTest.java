package com.knubisoft.testlum.testing.framework.exception;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link DefaultFrameworkException}. */
class DefaultFrameworkExceptionTest {

    @Nested
    class NoArgConstructor {
        @Test
        void messageIsNull() {
            DefaultFrameworkException ex = new DefaultFrameworkException();
            assertNull(ex.getMessage());
        }

        @Test
        void causeIsNull() {
            DefaultFrameworkException ex = new DefaultFrameworkException();
            assertNull(ex.getCause());
        }
    }

    @Nested
    class StringMessageConstructor {
        @Test
        void messageIsPreserved() {
            DefaultFrameworkException ex = new DefaultFrameworkException("something went wrong");
            assertEquals("something went wrong", ex.getMessage());
        }

        @Test
        void nullMessageIsAllowed() {
            DefaultFrameworkException ex = new DefaultFrameworkException((String) null);
            assertNull(ex.getMessage());
        }

        @Test
        void emptyMessageIsAllowed() {
            DefaultFrameworkException ex = new DefaultFrameworkException("");
            assertEquals("", ex.getMessage());
        }
    }

    @Nested
    class ListMessageConstructor {
        @Test
        void joinsMultipleMessages() {
            List<String> messages = Arrays.asList("error 1", "error 2", "error 3");
            DefaultFrameworkException ex = new DefaultFrameworkException(messages);
            String msg = ex.getMessage();
            assertNotNull(msg);
            assertTrue(msg.contains("error 1"));
            assertTrue(msg.contains("error 2"));
            assertTrue(msg.contains("error 3"));
        }

        @Test
        void singleMessage() {
            List<String> messages = Collections.singletonList("only one error");
            DefaultFrameworkException ex = new DefaultFrameworkException(messages);
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("only one error"));
        }

        @Test
        void messageStartsWithErrors() {
            List<String> messages = List.of("err");
            DefaultFrameworkException ex = new DefaultFrameworkException(messages);
            assertTrue(ex.getMessage().startsWith("Errors:"));
        }

        @Test
        void emptyListProducesErrorsPrefix() {
            List<String> messages = Collections.emptyList();
            DefaultFrameworkException ex = new DefaultFrameworkException(messages);
            assertTrue(ex.getMessage().startsWith("Errors:"));
        }
    }

    @Nested
    class FormattedMessageConstructor {
        @Test
        void formatsWithSingleArg() {
            DefaultFrameworkException ex = new DefaultFrameworkException("File %s not found", "test.xml");
            assertEquals("File test.xml not found", ex.getMessage());
        }

        @Test
        void formatsWithMultipleArgs() {
            DefaultFrameworkException ex = new DefaultFrameworkException(
                    "%s failed at line %d", "Parser", 42);
            assertEquals("Parser failed at line 42", ex.getMessage());
        }

        @Test
        void formatsWithNoArgs() {
            DefaultFrameworkException ex = new DefaultFrameworkException("no placeholders");
            assertEquals("no placeholders", ex.getMessage());
        }
    }

    @Nested
    class ThrowableCauseConstructor {
        @Test
        void causeIsPreserved() {
            IOException cause = new IOException("disk full");
            DefaultFrameworkException ex = new DefaultFrameworkException(cause);
            assertSame(cause, ex.getCause());
        }

        @Test
        void messageContainsCauseInfo() {
            IOException cause = new IOException("disk full");
            DefaultFrameworkException ex = new DefaultFrameworkException(cause);
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("disk full"));
        }

        @Test
        void nullCauseIsAllowed() {
            DefaultFrameworkException ex = new DefaultFrameworkException((Throwable) null);
            assertNull(ex.getCause());
        }
    }

    @Nested
    class MessageAndCauseConstructor {
        @Test
        void messageAndCauseArePreserved() {
            RuntimeException cause = new RuntimeException("root");
            DefaultFrameworkException ex = new DefaultFrameworkException("wrapper msg", cause);
            assertEquals("wrapper msg", ex.getMessage());
            assertSame(cause, ex.getCause());
        }

        @Test
        void nullMessageWithValidCause() {
            RuntimeException cause = new RuntimeException("root");
            DefaultFrameworkException ex = new DefaultFrameworkException(null, cause);
            assertNull(ex.getMessage());
            assertSame(cause, ex.getCause());
        }

        @Test
        void validMessageWithNullCause() {
            DefaultFrameworkException ex = new DefaultFrameworkException("msg", (Throwable) null);
            assertEquals("msg", ex.getMessage());
            assertNull(ex.getCause());
        }
    }

    @Nested
    class InheritanceHierarchy {
        @Test
        void isRuntimeException() {
            DefaultFrameworkException ex = new DefaultFrameworkException();
            assertInstanceOf(RuntimeException.class, ex);
        }

        @Test
        void canBeCaughtAsRuntimeException() {
            assertThrows(RuntimeException.class, () -> {
                throw new DefaultFrameworkException("fail");
            });
        }
    }
}
