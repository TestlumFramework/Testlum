package com.knubisoft.testlum.log.table;

import com.knubisoft.testlum.log.Color;
import com.knubisoft.testlum.log.LogFormat;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;

import java.util.ArrayList;
import java.util.List;

final class TableRenderer {

    private static final String NL = System.lineSeparator();
    private static final int HEADER_OFFSET = 1;
    private static final int TITLE_AND_HEADER_OFFSET = 2;

    private TableRenderer() {
    }

    static String render(final TableSpec spec) {
        validate(spec);
        AsciiTable table = constructTable(spec);
        table.getRenderer().setCWC(new CWC_LongestLine());
        table.setTextAlignment(spec.align().getAlignment());
        return colorize(NL.concat(table.render()), spec);
    }

    private static void validate(final TableSpec spec) {
        if (spec.headers() == null || spec.headers().length == 0) {
            throw new IllegalStateException("Table columns must be declared before build()");
        }
        validateRowWidths(spec.headers().length, spec.rows());
    }

    private static AsciiTable constructTable(final TableSpec spec) {
        AsciiTable table = new AsciiTable();
        table.addRule();
        addSingleSpan(table, textOf(spec.title()), spec.headers().length);
        table.addRow((Object[]) spec.headers());
        table.addRule();
        for (Row row : spec.rows()) {
            table.addRow(row.cells());
            table.addRule();
        }
        addSingleSpan(table, textOf(spec.footer()), spec.headers().length);
        return table;
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
        int dataOffset = spec.title() != null ? TITLE_AND_HEADER_OFFSET : HEADER_OFFSET;
        int segmentIdx = -1;
        for (int i = 0; i < lines.length; i++) {
            segmentIdx = appendLine(out, lines, i, spec, dataOffset, segmentIdx);
        }
        return out.toString();
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
        Color effective = pickColor(spec, nextIdx - dataOffset, rule);
        out.append(apply(lines[i], effective));
        if (i < lines.length - 1) {
            out.append(NL);
        }
        return nextIdx;
    }

    private static Color pickColor(final TableSpec spec, final int dataRowIdx, final boolean rule) {
        if (rule) {
            return spec.color();
        }
        Color specific = resolveSpecificColor(spec, dataRowIdx);
        return specific == null || specific == Color.NONE ? spec.color() : specific;
    }

    private static Color resolveSpecificColor(final TableSpec spec, final int dataRowIdx) {
        if (dataRowIdx == -TITLE_AND_HEADER_OFFSET && spec.title() != null) {
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
