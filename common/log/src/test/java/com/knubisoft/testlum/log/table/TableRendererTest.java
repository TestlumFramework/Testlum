package com.knubisoft.testlum.log.table;

import com.knubisoft.testlum.log.Color;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableRendererTest {

    private static final int MAX_TABLE_WIDTH = 120;

    private static final String ESCAPE = "";
    private static final String ANSI_PATTERN = ESCAPE + "\\[[0-9;]*m";

    private static final String LONG_REASON =
            "{ERROR=[XSDIssue{message='cvc-minLength-valid: Value 'Get all' with length = '7' is not facet-valid "
                    + "with respect to minLength '10' for type 'stringMin10'.', lineNumber=16, columnNumber=38, "
                    + "path=/Users/bodyashah/TestlumProjects/demo/resources/scenarios/invalid2/scenario.xml}]}";

    private List<String> plainLines(final String rendered) {
        return Arrays.stream(rendered.replaceAll(ANSI_PATTERN, "").split("\\R"))
                .filter(line -> !line.isEmpty())
                .toList();
    }

    private int widestLine(final String rendered) {
        return plainLines(rendered).stream().mapToInt(String::length).max().orElse(0);
    }

    private String reasonTable(final String reason) {
        return TableBuilder.grid("Invalid scenarios (expected to run but could not)")
                .columns("Scenario", "Reason")
                .row(Color.RED, "/invalid2/scenario.xml", reason)
                .color(Color.RED)
                .align(Align.CENTER)
                .build();
    }

    @Nested
    class WithinTheCap {

        @Test
        void narrowTableIsNotPaddedOutToTheCap() {
            String rendered = TableBuilder.grid("Tag Configuration")
                    .columns("Tag", "Enabled", "Scenarios")
                    .row("smoke", true, 12)
                    .build();

            assertTrue(widestLine(rendered) < MAX_TABLE_WIDTH);
        }

        @Test
        void narrowTableIsNotWrapped() {
            String rendered = TableBuilder.grid("Tests failed")
                    .columns("Status", "Counts")
                    .footer("Test run finished after 0m 12s")
                    .row("Successful", 5)
                    .build();

            assertEquals(9, plainLines(rendered).size());
        }

        @Test
        void widthIsDrivenByTheLongestCell() {
            String rendered = TableBuilder.grid("Tag Configuration")
                    .columns("Tag", "Enabled", "Scenarios")
                    .row("regression", false, 5)
                    .build();

            assertTrue(rendered.contains("regression"));
        }
    }

    @Nested
    class BeyondTheCap {

        @Test
        void aLongCellIsWrappedInsteadOfWideningTheTable() {
            assertEquals(MAX_TABLE_WIDTH, widestLine(reasonTable(LONG_REASON)));
        }

        @Test
        void everyRenderedLineStaysWithinTheCap() {
            List<String> lines = plainLines(reasonTable(LONG_REASON));

            assertTrue(lines.stream().allMatch(line -> line.length() <= MAX_TABLE_WIDTH));
        }

        @Test
        void theLongCellSpansSeveralLines() {
            assertTrue(plainLines(reasonTable(LONG_REASON)).size() > 7);
        }

        @Test
        void contentIsNotLost() {
            String rendered = reasonTable(LONG_REASON);

            assertTrue(rendered.contains("cvc-minLength-valid"));
            assertTrue(rendered.contains("/invalid2/scenario.xml"));
        }

        @Test
        void shortColumnKeepsItsNaturalWidthWhileTheWideOneShrinks() {
            String firstDataLine = plainLines(reasonTable(LONG_REASON)).stream()
                    .filter(line -> line.startsWith("│/invalid2"))
                    .findFirst()
                    .orElseThrow();

            assertEquals("│/invalid2/scenario.xml│", firstDataLine.substring(0, 24));
        }

        @Test
        void anUnbrokenTokenIsHardSplitRatherThanOverflowing() {
            String path = "/Users/bodyashah/TestlumProjects/demo/resources/scenarios/deeply/nested/"
                    + "very/long/directory/chain/without/any/spaces/at/all/scenario.xml";

            assertEquals(MAX_TABLE_WIDTH, widestLine(reasonTable(path)));
        }

        @Test
        void manyColumnsDoNotBreakTheRenderer() {
            DynamicTableBuilder table = TableBuilder.grid("Wide").columnCount(8);
            Object[] cells = new Object[8];
            Arrays.fill(cells, LONG_REASON);
            table.row(cells);

            assertTrue(widestLine(table.build()) <= MAX_TABLE_WIDTH);
        }
    }

    @Nested
    class Colouring {

        /**
         * The colouring maps rendered lines onto rows by counting the rules between them, so a row
         * that wraps onto several lines must keep its colour on every one of them. Rule lines are
         * left untouched in this mode, hence only content lines are inspected.
         */
        @Test
        void everyWrappedLineOfARowKeepsTheRowColour() {
            long uncoloured = Arrays.stream(reasonTable(LONG_REASON).split("\\R"))
                    .filter(line -> line.replaceAll(ANSI_PATTERN, "").startsWith("│"))
                    .filter(line -> !line.contains(ESCAPE))
                    .count();

            assertEquals(0, uncoloured);
        }

        @Test
        void continuationLinesOfAColouredRowAreColoured() {
            long continuationLines = Arrays.stream(reasonTable(LONG_REASON).split("\\R"))
                    .filter(line -> line.contains(ESCAPE))
                    .filter(line -> line.replaceAll(ANSI_PATTERN, "").startsWith("│  "))
                    .count();

            assertTrue(continuationLines > 1);
        }

        @Test
        void titleAndFooterStillSpanTheWholeTable() {
            String rendered = TableBuilder.grid("Invalid scenarios (expected to run but could not)")
                    .columns("Scenario", "Reason")
                    .footer("done")
                    .row("/a/scenario.xml", LONG_REASON)
                    .build();
            List<String> lines = plainLines(rendered);

            assertTrue(lines.get(1).contains("Invalid scenarios"));
            assertTrue(lines.get(lines.size() - 2).contains("done"));
        }

        @Test
        void spanRowsSurviveWrapping() {
            String rendered = TableBuilder.grid("Tag Configuration")
                    .columnCount(3)
                    .span("Not declared in config")
                    .row("smoke_2", "-", 1)
                    .row("verbose", "-", LONG_REASON)
                    .build();

            assertTrue(rendered.contains("Not declared in config"));
            assertTrue(widestLine(rendered) <= MAX_TABLE_WIDTH);
        }
    }
}
