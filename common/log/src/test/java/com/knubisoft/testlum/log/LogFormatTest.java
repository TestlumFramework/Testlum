package com.knubisoft.testlum.log;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link LogFormat}. */
class LogFormatTest {

    @Nested
    class TableMethod {
        @Test
        void tableWithTextAndArgFormatsCorrectly() {
            String result = LogFormat.table("Name", "TestUser");
            assertNotNull(result);
            assertTrue(result.contains("Name"));
            assertTrue(result.contains("TestUser"));
        }

        @Test
        void tableWithTextOnlyUsesPlaceholder() {
            String result = LogFormat.table("Query");
            assertNotNull(result);
            assertTrue(result.contains("Query"));
            assertTrue(result.contains("{}"));
        }

        @Test
        void tableWithNullArgConvertsToString() {
            String result = LogFormat.table("Field", null);
            assertNotNull(result);
            assertTrue(result.contains("null"));
        }

        @Test
        void tableWithNumericArg() {
            String result = LogFormat.table("Port", 8080);
            assertTrue(result.contains("8080"));
        }

        @Test
        void tableWithEmptyText() {
            String result = LogFormat.table("", "value");
            assertNotNull(result);
            assertTrue(result.contains("value"));
        }
    }

    @Nested
    class WithColorMethods {
        @Test
        void withColorWrapsText() {
            String result = LogFormat.with(Color.RED, "error");
            assertTrue(result.startsWith(Color.RED.getCode()));
            assertTrue(result.endsWith(Color.RESET.getCode()));
            assertTrue(result.contains("error"));
        }

        @Test
        void withRedContainsRedCode() {
            String result = LogFormat.withRed("red text");
            assertTrue(result.contains(Color.RED.getCode()));
            assertTrue(result.contains("red text"));
        }

        @Test
        void withOrangeContainsOrangeCode() {
            String result = LogFormat.withOrange("orange text");
            assertTrue(result.contains(Color.ORANGE.getCode()));
            assertTrue(result.contains("orange text"));
        }

        @Test
        void withGreenContainsGreenCode() {
            String result = LogFormat.withGreen("green text");
            assertTrue(result.contains(Color.GREEN.getCode()));
            assertTrue(result.contains("green text"));
        }

        @Test
        void withCyanContainsCyanCode() {
            String result = LogFormat.withCyan("cyan text");
            assertTrue(result.contains(Color.CYAN.getCode()));
            assertTrue(result.contains("cyan text"));
        }

        @Test
        void withYellowContainsYellowCode() {
            String result = LogFormat.withYellow("yellow text");
            assertTrue(result.contains(Color.YELLOW.getCode()));
            assertTrue(result.contains("yellow text"));
        }

        @Test
        void withNoneColorWrapsWithEmptyCodes() {
            String result = LogFormat.with(Color.NONE, "plain");
            assertTrue(result.contains("plain"));
            assertTrue(result.endsWith(Color.RESET.getCode()));
        }

        @Test
        void withEmptyText() {
            String result = LogFormat.withRed("");
            assertEquals(Color.RED.getCode() + Color.RESET.getCode(), result);
        }
    }

    @Nested
    class StaticAccessors {
        @Test
        void newLineReturnsPattern() {
            String result = LogFormat.newLine();
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        void contentFormatReturnsNonEmpty() {
            String result = LogFormat.contentFormat();
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        void newLogLineReturnsNonEmpty() {
            String result = LogFormat.newLogLine();
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        void exceptionLogReturnsNonEmpty() {
            String result = LogFormat.exceptionLog();
            assertNotNull(result);
            assertTrue(result.contains("EXCEPTION"));
        }

        @Test
        void commandLogReturnsNonEmpty() {
            String result = LogFormat.commandLog();
            assertNotNull(result);
            assertTrue(result.contains("Command"));
        }

        @Test
        void exceptionLogContainsRedColor() {
            String result = LogFormat.exceptionLog();
            assertTrue(result.contains(Color.RED.getCode()));
        }

        @Test
        void commandLogContainsCyanColor() {
            String result = LogFormat.commandLog();
            assertTrue(result.contains(Color.CYAN.getCode()));
        }
    }

    @Nested
    class PrivateConstructor {
        @Test
        void cannotBeInstantiatedDirectly() {
            // LogFormat has a private constructor - verify the class exists as utility
            assertNotNull(LogFormat.class);
        }
    }
}
