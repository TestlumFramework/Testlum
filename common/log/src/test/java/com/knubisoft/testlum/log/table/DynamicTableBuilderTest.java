package com.knubisoft.testlum.log.table;

import com.knubisoft.testlum.log.Color;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicTableBuilderTest {

    @Nested
    class Basic {

        @Test
        void rendersHeadersAndRows() {
            String out = TableBuilder.grid("Failed scenarios")
                    .columns("Scenario", "Reason")
                    .row("/foo.xml", "integration disabled")
                    .row("/bar.xml", "parse error")
                    .build();
            assertTrue(out.contains("Failed scenarios"));
            assertTrue(out.contains("/foo.xml"));
            assertTrue(out.contains("parse error"));
        }

        @Test
        void footerIsRendered() {
            String out = TableBuilder.grid()
                    .columns("a", "b")
                    .row("1", "2")
                    .footer("3 total")
                    .build();
            assertTrue(out.contains("3 total"));
        }
    }

    @Nested
    class PerRowColor {

        @Test
        void onlyTargetedRowIsColored() {
            String out = TableBuilder.grid()
                    .columns("Status", "Count")
                    .row("Passed", 7)
                    .row(Color.RED, "Failed", 2)
                    .build();
            int redOpen = out.indexOf(Color.RED.getCode());
            assertTrue(redOpen >= 0, "Failed row must carry RED ANSI code");
            assertTrue(out.indexOf("Passed") < redOpen,
                    "Passed row must appear before the first RED code");
        }

        @Test
        void noneRowFallsBackToTableWideColor() {
            String out = TableBuilder.grid()
                    .columns("a")
                    .row(Color.NONE, "x")
                    .color(Color.CYAN)
                    .build();
            assertTrue(out.contains(Color.CYAN.getCode()));
        }

        @Test
        void explicitRowColorOverridesTableWide() {
            String out = TableBuilder.grid()
                    .columns("a")
                    .row(Color.RED, "x")
                    .color(Color.CYAN)
                    .build();
            assertTrue(out.contains(Color.RED.getCode()));
        }

        @Test
        void rulesUseTableWideColorNotRowColor() {
            String out = TableBuilder.grid()
                    .columns("a")
                    .row(Color.RED, "x")
                    .build();
            String[] lines = out.split("\\R");
            for (String line : lines) {
                boolean isRuleLine = !line.isEmpty() && !line.contains("│") && !line.contains("|");
                if (isRuleLine) {
                    assertFalse(line.contains(Color.RED.getCode()),
                            "Rule lines must not carry a per-row color: " + line);
                }
            }
        }

        @Test
        void noColorsProduceNoAnsi() {
            String out = TableBuilder.grid()
                    .columns("a")
                    .row("x")
                    .build();
            assertFalse(out.contains("\u001B["));
        }

        @Test
        void tableWideColorAloneColorsBorders() {
            String out = TableBuilder.grid()
                    .columns("a")
                    .row("x")
                    .color(Color.CYAN)
                    .build();
            String[] lines = out.split("\\R");
            boolean coloredRuleFound = false;
            for (String line : lines) {
                boolean isRule = !line.isEmpty() && !line.contains("│") && !line.contains("|");
                if (isRule && line.contains(Color.CYAN.getCode())) {
                    coloredRuleFound = true;
                    break;
                }
            }
            assertTrue(coloredRuleFound, "Rule lines must carry CYAN when only table-wide color is set");
        }

        @Test
        void tableWideColorYieldsToSurgicalModeWhenRowColorSet() {
            String out = TableBuilder.grid()
                    .columns("a")
                    .row(Color.RED, "x")
                    .color(Color.CYAN)
                    .build();
            String[] lines = out.split("\\R");
            for (String line : lines) {
                boolean isRule = !line.isEmpty() && !line.contains("│") && !line.contains("|");
                if (isRule) {
                    assertFalse(line.contains(Color.CYAN.getCode()),
                            "Rules must be neutral in surgical mode: " + line);
                }
            }
        }

        @Test
        void separatorsStayOutsideAnsiWrap() {
            String out = TableBuilder.grid()
                    .columns("a", "b")
                    .row(Color.RED, "x", "y")
                    .build();
            int open = out.indexOf(Color.RED.getCode());
            int reset = out.indexOf(Color.RESET.getCode(), open);
            assertTrue(open >= 0 && reset > open);
            String wrapped = out.substring(open + Color.RED.getCode().length(), reset);
            assertFalse(wrapped.contains("│"),
                    "Cell separator must not be inside ANSI wrap: '" + wrapped + "'");
            assertFalse(wrapped.contains("|"),
                    "Cell separator must not be inside ANSI wrap: '" + wrapped + "'");
        }

        @Test
        void bulkRowsAreUncolored() {
            String out = TableBuilder.grid()
                    .columns("a", "b")
                    .rows(java.util.List.of(new Object[]{"1", "2"}, new Object[]{"3", "4"}))
                    .build();
            assertFalse(out.contains("\u001B["));
        }
    }

    @Nested
    class TitleAndFooterColors {

        @Test
        void titleColorColorsOnlyTitleText() {
            String out = TableBuilder.grid("Hi")
                    .titleColor(Color.CYAN)
                    .columns("a")
                    .row("x")
                    .build();
            String[] lines = out.split("\\R");
            String titleLine = findLineContaining(lines, "Hi");
            assertTrue(titleLine.contains(Color.CYAN.getCode()));
            String dataLine = findLineContaining(lines, "x");
            assertFalse(dataLine.contains(Color.CYAN.getCode()),
                    "Data row must not pick up title color");
        }

        @Test
        void footerColorColorsOnlyFooterText() {
            String out = TableBuilder.grid()
                    .columns("a")
                    .row("x")
                    .footer(Color.YELLOW, "end")
                    .build();
            String[] lines = out.split("\\R");
            String footerLine = findLineContaining(lines, "end");
            assertTrue(footerLine.contains(Color.YELLOW.getCode()));
            String dataLine = findLineContaining(lines, "x");
            assertFalse(dataLine.contains(Color.YELLOW.getCode()),
                    "Data row must not pick up footer color");
        }

        @Test
        void titleAndFooterFallBackToTableWideColor() {
            String out = TableBuilder.grid("Hi")
                    .columns("a")
                    .row("x")
                    .footer("end")
                    .color(Color.CYAN)
                    .build();
            String[] lines = out.split("\\R");
            assertTrue(findLineContaining(lines, "Hi").contains(Color.CYAN.getCode()));
            assertTrue(findLineContaining(lines, "end").contains(Color.CYAN.getCode()));
        }

        @Test
        void titleAndFooterOverrideTableWideColor() {
            String out = TableBuilder.grid("Hi")
                    .titleColor(Color.GREEN)
                    .columns("a")
                    .row("x")
                    .footer(Color.RED, "end")
                    .color(Color.CYAN)
                    .build();
            String[] lines = out.split("\\R");
            assertTrue(findLineContaining(lines, "Hi").contains(Color.GREEN.getCode()));
            assertFalse(findLineContaining(lines, "Hi").contains(Color.CYAN.getCode()));
            assertTrue(findLineContaining(lines, "end").contains(Color.RED.getCode()));
            assertFalse(findLineContaining(lines, "end").contains(Color.CYAN.getCode()));
        }

        @Test
        void titleColorWithoutTitleIsNoOp() {
            String out = TableBuilder.grid()
                    .titleColor(Color.CYAN)
                    .columns("a")
                    .row("x")
                    .build();
            assertFalse(out.contains(Color.CYAN.getCode()));
        }

        private String findLineContaining(final String[] lines, final String token) {
            for (String line : lines) {
                if (line.contains(token)) {
                    return line;
                }
            }
            throw new AssertionError("No line containing: " + token);
        }
    }

    @Nested
    class RowsWithTitle {

        @Test
        void dataRowIsColoredWhenTitlePresent() {
            String out = TableBuilder.grid("Hi")
                    .columns("a")
                    .row(Color.GREEN, "x")
                    .build();
            assertTrue(out.contains(Color.GREEN.getCode()));
        }

        @Test
        void titleLineIsNotColoredByRowColor() {
            String out = TableBuilder.grid("TitleText")
                    .columns("a")
                    .row(Color.GREEN, "x")
                    .build();
            String[] lines = out.split("\\R");
            for (String line : lines) {
                if (line.contains("TitleText")) {
                    assertFalse(line.contains(Color.GREEN.getCode()),
                            "Title must not carry row color");
                }
            }
        }
    }

    @Nested
    class Validation {

        @Test
        void mismatchedRowThrows() {
            IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> TableBuilder.grid().columns("a", "b").row("only-one").build());
            assertTrue(ex.getMessage().contains("Row 0"));
        }

        @Test
        void missingColumnsThrows() {
            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalStateException.class,
                    () -> TableBuilder.grid().row("x").build());
        }

        @Test
        void columnsOrderIsPreserved() {
            String out = TableBuilder.grid()
                    .columns("Col1", "Col2")
                    .row("1", "2")
                    .build();
            assertTrue(out.indexOf("Col2") > out.indexOf("Col1"));
        }
    }
}
