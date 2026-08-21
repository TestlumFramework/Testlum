package com.knubisoft.testlum.log.table;

import de.vandermeer.skb.interfaces.render.DoesRenderToWidth;

import java.util.ArrayList;
import java.util.List;

public final class MultilineCell implements DoesRenderToWidth {

    private static final char LINE_BREAK = '\n';

    private static final String LINE_BREAK_REGEX = "\\R";

    private static final String DOUBLE_SPACE = "  ";

    private static final int CONTINUATION_INDENT = 2;

    private static final int MAX_INDENT_RATIO = 2;

    private final String text;

    public MultilineCell(final String text) {
        this.text = text;
    }

    public static boolean needsVerbatimLayout(final Object cell) {
        if (!(cell instanceof CharSequence)) {
            return false;
        }
        String text = String.valueOf(cell);
        return text.split(LINE_BREAK_REGEX, -1).length > 1 || text.contains(DOUBLE_SPACE);
    }

    @Override
    public String render(final int width) {
        if (width < 1) {
            return text;
        }
        List<String> lines = new ArrayList<>();
        for (String logicalLine : text.split(LINE_BREAK_REGEX, -1)) {
            wrapLogicalLine(logicalLine, width, lines);
        }
        return join(lines, width);
    }

    @Override
    public String toString() {
        return text;
    }

    private static String join(final List<String> lines, final int width) {
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                joined.append(LINE_BREAK);
            }
            joined.append(pad(lines.get(i), width));
        }
        return joined.toString();
    }

    private static String pad(final String line, final int width) {
        if (line.length() > width) {
            return line.substring(0, width);
        }
        return line + " ".repeat(width - line.length());
    }

    private static void wrapLogicalLine(final String line, final int width, final List<String> out) {
        String indent = continuationIndent(line, width);
        String remainder = line;
        while (remainder.length() > width) {
            int cut = breakPoint(remainder, width, indent.length());
            out.add(remainder.substring(0, cut).stripTrailing());
            remainder = indent + remainder.substring(cut).stripLeading();
        }
        out.add(remainder);
    }

    private static String continuationIndent(final String line, final int width) {
        int leading = 0;
        while (leading < line.length() && line.charAt(leading) == ' ') {
            leading++;
        }
        return " ".repeat(Math.min(leading + CONTINUATION_INDENT, width / MAX_INDENT_RATIO));
    }

    private static int breakPoint(final String line, final int width, final int indentLength) {
        int space = line.lastIndexOf(' ', width);
        return space > indentLength ? space : width;
    }
}
