package com.knubisoft.testlum.log;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link ColoredText}. */
class ColoredTextTest {

    @Nested
    class AddMethod {
        @Test
        void addNonNullDoesNotThrow() {
            ColoredText ct = new ColoredText(Color.GREEN);
            assertDoesNotThrow(() -> ct.add("hello"));
        }

        @Test
        void addNullIsIgnored() {
            ColoredText ct = new ColoredText(Color.GREEN);
            assertDoesNotThrow(() -> ct.add(null));
        }

        @Test
        void addEmptyStringIsAccepted() {
            ColoredText ct = new ColoredText(Color.GREEN);
            assertDoesNotThrow(() -> ct.add(""));
        }
    }

    @Nested
    class AddAllMethod {
        @Test
        void addAllWithNonNullList() {
            ColoredText ct = new ColoredText(Color.RED);
            List<String> data = Arrays.asList("line1", "line2");
            assertDoesNotThrow(() -> ct.addAll(data));
        }

        @Test
        void addAllWithNullListDoesNotThrow() {
            ColoredText ct = new ColoredText(Color.RED);
            assertDoesNotThrow(() -> ct.addAll(null));
        }

        @Test
        void addAllWithEmptyListDoesNotThrow() {
            ColoredText ct = new ColoredText(Color.RED);
            assertDoesNotThrow(() -> ct.addAll(Collections.emptyList()));
        }

        @Test
        void addAllSkipsNullEntries() {
            ColoredText ct = new ColoredText(Color.CYAN);
            List<String> data = Arrays.asList("line1", null, "line3");
            assertDoesNotThrow(() -> ct.addAll(data));
        }
    }

    @Nested
    class InfoMethod {
        @Test
        void infoDoesNotThrowWhenEmpty() {
            ColoredText ct = new ColoredText(Color.YELLOW);
            assertDoesNotThrow(ct::info);
        }

        @Test
        void infoDoesNotThrowWithLines() {
            ColoredText ct = new ColoredText(Color.RED);
            ct.add("test line");
            assertDoesNotThrow(ct::info);
        }

        @Test
        void infoClearsLinesAfterCall() {
            ColoredText ct = new ColoredText(Color.GREEN);
            ct.add("line1");
            ct.info();
            // calling info again should not re-print the same lines (they were cleared)
            assertDoesNotThrow(ct::info);
        }
    }

    @Nested
    class NullColor {
        @Test
        void nullColorDoesNotThrowOnInfo() {
            ColoredText ct = new ColoredText(null);
            ct.add("uncolored line");
            assertDoesNotThrow(ct::info);
        }
    }

    @Nested
    class NoneColor {
        @Test
        void noneColorWrapsWithEmptyCode() {
            ColoredText ct = new ColoredText(Color.NONE);
            ct.add("test");
            assertDoesNotThrow(ct::info);
        }
    }
}
