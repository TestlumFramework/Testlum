package com.knubisoft.testlum.log;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link Color} enum. */
class ColorTest {

    @Nested
    class AnsiCodes {
        @Test
        void redHasAnsiCode() {
            assertEquals("\u001B[31m", Color.RED.getCode());
        }

        @Test
        void orangeHasAnsiCode() {
            assertEquals("\u001b[38;5;208m", Color.ORANGE.getCode());
        }

        @Test
        void greenHasAnsiCode() {
            assertEquals("\u001B[32m", Color.GREEN.getCode());
        }

        @Test
        void cyanHasAnsiCode() {
            assertEquals("\u001b[36m", Color.CYAN.getCode());
        }

        @Test
        void resetHasAnsiCode() {
            assertEquals("\u001b[0m", Color.RESET.getCode());
        }

        @Test
        void yellowHasAnsiCode() {
            assertEquals("\u001B[33m", Color.YELLOW.getCode());
        }

        @Test
        void noneHasEmptyCode() {
            assertEquals("", Color.NONE.getCode());
        }
    }

    @Nested
    class EnumValues {
        @Test
        void allValuesExist() {
            Color[] values = Color.values();
            assertEquals(7, values.length);
        }

        @Test
        void valueOfWorks() {
            assertEquals(Color.RED, Color.valueOf("RED"));
            assertEquals(Color.NONE, Color.valueOf("NONE"));
        }

        @Test
        void valueOfInvalidThrows() {
            assertThrows(IllegalArgumentException.class, () -> Color.valueOf("PURPLE"));
        }
    }

    @Nested
    class CodeNonNullity {
        @Test
        void allCodesAreNonNull() {
            for (Color color : Color.values()) {
                assertNotNull(color.getCode(), "Code should not be null for " + color);
            }
        }
    }
}
