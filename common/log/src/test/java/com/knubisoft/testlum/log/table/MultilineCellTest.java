package com.knubisoft.testlum.log.table;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MultilineCell#render(int)} feeds AsciiTable directly, which trusts the returned lines to be exactly
 * the requested width - anything wider breaks the table frame, anything narrower misaligns its borders.
 */
class MultilineCellTest {

    private static final int WIDTH = 30;

    @Test
    void everyRenderedLineIsExactlyTheRequestedWidth() {
        List<String> lines = render("line 16  <postgres comment=\"Get all\">\n"
                + "   x  'comment' is too short: 7 characters, minimum is 10", WIDTH);
        assertTrue(lines.stream().allMatch(line -> line.length() == WIDTH), lines.toString());
    }

    @Test
    void declaredLineBreaksAreKept() {
        assertEquals(3, render("one\ntwo\nthree", WIDTH).size());
    }

    @Test
    void blankLinesAreKeptAsBlankLines() {
        List<String> lines = render("first\n\nsecond", WIDTH);
        assertEquals(3, lines.size());
        assertTrue(lines.get(1).isBlank());
    }

    @Test
    void carriageReturnsCountAsLineBreaks() {
        assertEquals(2, render("one\r\ntwo", WIDTH).size());
    }

    @Test
    void longLinesWrapOnWordBoundaries() {
        List<String> lines = render("the quick brown fox jumps over the lazy dog", 20);
        assertTrue(lines.size() > 1);
        assertFalse(lines.get(0).contains("quic\n"));
        assertTrue(String.join(" ", lines).contains("quick"));
    }

    @Test
    void wrappedContinuationsAreIndentedUnderTheirLine() {
        List<String> lines = render("   x  'comment' is far too short to fit into this narrow column", 24);
        assertTrue(lines.size() > 1);
        assertTrue(lines.get(1).startsWith("     "), lines.get(1));
    }

    /**
     * A path or an unbroken value can be longer than the whole column; it has to be cut rather than allowed to
     * push the frame open.
     */
    @Test
    void unbreakableTokensAreHardSplit() {
        String token = "a".repeat(90);
        List<String> lines = render(token, 20);
        assertTrue(lines.stream().allMatch(line -> line.length() == 20));
        assertEquals(token, String.join("", lines).replace(" ", ""));
    }

    @Test
    void noContentIsLostWhileWrapping() {
        String text = "attribute 'comment' of element 'postgres' is not valid";
        String rejoined = String.join(" ", render(text, 18)).replaceAll("\\s+", " ").strip();
        assertEquals(text, rejoined);
    }

    @Test
    void toStringKeepsTheOriginalTextSoColumnWidthsCanBeMeasured() {
        String text = "one\ntwo";
        assertEquals(text, new MultilineCell(text).toString());
    }

    @Test
    void nonPositiveWidthReturnsTheTextUntouched() {
        assertEquals("one\ntwo", new MultilineCell("one\ntwo").render(0));
    }

    /**
     * Ordinary cells must keep going through AsciiTable so the table's text alignment still applies to them.
     */
    @Test
    void onlyTextCarryingItsOwnLayoutNeedsThisCell() {
        assertTrue(MultilineCell.needsVerbatimLayout("one\ntwo"));
        assertTrue(MultilineCell.needsVerbatimLayout("line 16  <postgres>"));
        assertFalse(MultilineCell.needsVerbatimLayout("one line"));
        assertFalse(MultilineCell.needsVerbatimLayout(null));
        assertFalse(MultilineCell.needsVerbatimLayout(42));
    }

    private static List<String> render(final String text, final int width) {
        return Arrays.asList(new MultilineCell(text).render(width).split("\n", -1));
    }
}
