package com.knubisoft.testlum.log.table;

import com.knubisoft.testlum.log.Color;
import com.knubisoft.testlum.log.LogFormat;
import de.vandermeer.asciitable.AT_ColumnWidthCalculator;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_FixedWidth;
import de.vandermeer.asciitable.CWC_LongestLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

final class TableRenderer {

    private static final String NL = System.lineSeparator();

    private static final int MAX_TABLE_WIDTH = 120;

    private static final int MIN_COLUMN_WIDTH = 5;

    private TableRenderer() {
    }

    static String render(final TableSpec spec) {
        validate(spec);
        AsciiTable table = constructTable(spec);
        table.getRenderer().setCWC(computeWidthCalculator(spec));
        table.setTextAlignment(spec.align().getAlignment());
        return colorize(NL.concat(table.render()), spec);
    }

    private static AT_ColumnWidthCalculator computeWidthCalculator(final TableSpec spec) {
        int columnCount = spec.columnCount();
        int budget = MAX_TABLE_WIDTH - (columnCount + 1);
        if (budget < columnCount * MIN_COLUMN_WIDTH) {
            return new CWC_LongestLine();
        }
        int[] natural = measureNaturalWidths(spec);
        if (sum(natural) <= budget) {
            return new CWC_LongestLine();
        }
        return toFixedWidth(shrinkToBudget(natural, budget));
    }

    private static CWC_FixedWidth toFixedWidth(final int[] widths) {
        CWC_FixedWidth calculator = new CWC_FixedWidth();
        for (int width : widths) {
            calculator.add(width);
        }
        return calculator;
    }

    private static int[] measureNaturalWidths(final TableSpec spec) {
        int[] widths = new int[spec.columnCount()];
        for (Row row : spec.rows()) {
            measureRow(widths, row.cells());
        }
        if (spec.headers() != null) {
            measureRow(widths, spec.headers());
        }
        measureLastColumn(widths, textOf(spec.title()));
        measureLastColumn(widths, textOf(spec.footer()));
        return widths;
    }

    private static void measureRow(final int[] widths, final Object[] cells) {
        for (int i = 0; i < cells.length && i < widths.length; i++) {
            widths[i] = Math.max(widths[i], longestLine(cells[i]));
        }
    }

    private static void measureLastColumn(final int[] widths, final String text) {
        if (text == null) {
            return;
        }
        int last = widths.length - 1;
        widths[last] = Math.max(widths[last], longestLine(text));
    }

    private static int longestLine(final Object cell) {
        if (cell == null) {
            return 0;
        }
        int longest = 0;
        for (String line : String.valueOf(cell).split("\\R", -1)) {
            longest = Math.max(longest, line.length());
        }
        return longest;
    }

    private static int[] shrinkToBudget(final int[] natural, final int budget) {
        int[] widths = new int[natural.length];
        int remaining = budget;
        int left = natural.length;
        for (Integer index : narrowestFirst(natural)) {
            int fair = remaining / left;
            widths[index] = Math.max(MIN_COLUMN_WIDTH, Math.min(natural[index], fair));
            remaining -= widths[index];
            left--;
        }
        return widths;
    }

    private static Integer[] narrowestFirst(final int[] natural) {
        Integer[] order = new Integer[natural.length];
        for (int i = 0; i < natural.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingInt(index -> natural[index]));
        return order;
    }

    private static int sum(final int[] values) {
        int total = 0;
        for (int value : values) {
            total += value;
        }
        return total;
    }

    private static void validate(final TableSpec spec) {
        if (spec.columnCount() <= 0) {
            throw new IllegalStateException("Table columns must be declared before build()");
        }
        validateRowWidths(spec.columnCount(), spec.rows());
    }

    private static AsciiTable constructTable(final TableSpec spec) {
        AsciiTable table = new AsciiTable();
        table.addRule();
        addSingleSpan(table, textOf(spec.title()), spec.columnCount());
        addHeaderRow(table, spec);
        for (Row row : spec.rows()) {
            table.addRow(row.cells());
            table.addRule();
        }
        addSingleSpan(table, textOf(spec.footer()), spec.columnCount());
        return table;
    }

    private static void addHeaderRow(final AsciiTable table, final TableSpec spec) {
        if (spec.headers() == null) {
            return;
        }
        table.addRow((Object[]) spec.headers());
        table.addRule();
    }

    private static String textOf(final Caption caption) {
        return caption == null ? null : caption.text();
    }

    private static void addSingleSpan(final AsciiTable table, final String text, final int tableLength) {
        if (text == null) {
            return;
        }
        ArrayList<Object> span = new ArrayList<>(tableLength);
        for (int i = 0; i < tableLength - 1; i++) {
            span.add(null);
        }
        span.add(text);
        table.addRow(span);
        table.addRule();
    }

    private static String colorize(final String body, final TableSpec spec) {
        return hasElementColors(spec) ? surgical(body, spec) : uniform(body, spec.color());
    }

    private static String surgical(final String body, final TableSpec spec) {
        String[] lines = body.split("\\R", -1);
        StringBuilder out = new StringBuilder();
        int dataOffset = computeDataOffset(spec);
        int segmentIdx = -1;
        for (int i = 0; i < lines.length; i++) {
            segmentIdx = appendLine(out, lines, i, spec, dataOffset, segmentIdx);
        }
        return out.toString();
    }

    private static int computeDataOffset(final TableSpec spec) {
        int offset = 0;
        if (spec.title() != null) {
            offset++;
        }
        if (spec.headers() != null) {
            offset++;
        }
        return offset;
    }

    private static String uniform(final String body, final Color color) {
        if (color == null || color == Color.NONE) {
            return body;
        }
        String[] lines = body.split("\\R", -1);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            out.append(lines[i].isEmpty() ? lines[i] : LogFormat.with(color, lines[i]));
            if (i < lines.length - 1) {
                out.append(NL);
            }
        }
        return out.toString();
    }

    private static boolean hasElementColors(final TableSpec spec) {
        if (hasColor(spec.title()) || hasColor(spec.footer())) {
            return true;
        }
        for (Row row : spec.rows()) {
            if (isSet(row.color())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasColor(final Caption caption) {
        return caption != null && isSet(caption.color());
    }

    private static boolean isSet(final Color color) {
        return color != null && color != Color.NONE;
    }

    private static int appendLine(final StringBuilder out, final String[] lines, final int i,
                                  final TableSpec spec, final int dataOffset, final int segmentIdx) {
        boolean rule = isRule(lines[i]);
        int nextIdx = rule ? segmentIdx + 1 : segmentIdx;
        Color effective = pickColor(spec, nextIdx - dataOffset, dataOffset, rule);
        out.append(apply(lines[i], effective));
        if (i < lines.length - 1) {
            out.append(NL);
        }
        return nextIdx;
    }

    private static Color pickColor(final TableSpec spec, final int dataRowIdx,
                                   final int dataOffset, final boolean rule) {
        if (rule) {
            return spec.color();
        }
        Color specific = resolveSpecificColor(spec, dataRowIdx, dataOffset);
        return specific == null || specific == Color.NONE ? spec.color() : specific;
    }

    private static Color resolveSpecificColor(final TableSpec spec, final int dataRowIdx,
                                              final int dataOffset) {
        if (dataRowIdx == -dataOffset && spec.title() != null) {
            return spec.title().color();
        }
        if (dataRowIdx == spec.rows().size() && spec.footer() != null) {
            return spec.footer().color();
        }
        if (dataRowIdx >= 0 && dataRowIdx < spec.rows().size()) {
            return spec.rows().get(dataRowIdx).color();
        }
        return Color.NONE;
    }

    private static boolean isRule(final String line) {
        if (line.isEmpty()) {
            return false;
        }
        char first = line.charAt(0);
        return first != '│' && first != '|';
    }

    private static String apply(final String line, final Color color) {
        if (color == null || color == Color.NONE || !hasSeparator(line)) {
            return line;
        }
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        while (pos < line.length()) {
            pos = appendSegment(sb, line, pos, color);
        }
        return sb.toString();
    }

    private static int appendSegment(final StringBuilder sb, final String line,
                                     final int pos, final Color color) {
        int next = nextSeparator(line, pos);
        if (pos == next) {
            sb.append(line.charAt(pos));
            return pos + 1;
        }
        sb.append(LogFormat.with(color, line.substring(pos, next)));
        return next;
    }

    private static boolean hasSeparator(final String line) {
        return line.indexOf('│') >= 0 || line.indexOf('|') >= 0;
    }

    private static int nextSeparator(final String line, final int from) {
        for (int i = from; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '│' || c == '|') {
                return i;
            }
        }
        return line.length();
    }

    private static void validateRowWidths(final int expected, final List<Row> rows) {
        for (int i = 0; i < rows.size(); i++) {
            int actual = rows.get(i).cells().length;
            if (actual != expected) {
                throw new IllegalArgumentException(
                        "Row " + i + " has " + actual + " cells but table has " + expected + " columns"
                );
            }
        }
    }
}
