package com.testlum.log.table;

import com.testlum.log.Color;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class DynamicTableBuilder {

    @Getter
    private final List<Row> rows = new ArrayList<>();
    private Caption title;
    private Caption footer;
    private String[] headers;
    private int columnCount;
    private Align align = Align.CENTER;
    private Color color = Color.NONE;

    DynamicTableBuilder(final String titleText) {
        this.title = titleText == null ? null : new Caption(titleText, Color.NONE);
    }

    public DynamicTableBuilder titleColor(final Color titleColor) {
        if (this.title == null) {
            return this;
        }
        this.title = new Caption(this.title.text(), titleColor);
        return this;
    }

    public DynamicTableBuilder columns(final String... columnHeaders) {
        this.requireColumnsNotDeclared();
        if (columnHeaders == null || columnHeaders.length == 0) {
            throw new IllegalArgumentException("Table column headers must not be empty");
        }
        this.headers = columnHeaders;
        this.columnCount = columnHeaders.length;
        return this;
    }

    public DynamicTableBuilder columnCount(final int count) {
        this.requireColumnsNotDeclared();
        if (count <= 0) {
            throw new IllegalArgumentException("Table column count must be positive but was " + count);
        }
        this.columnCount = count;
        return this;
    }

    public DynamicTableBuilder span(final String text) {
        return this.span(Color.NONE, text);
    }

    public DynamicTableBuilder span(final Color spanColor, final String text) {
        if (this.columnCount <= 0) {
            throw new IllegalStateException("Table columns must be declared before a span row is added");
        }
        Object[] cells = new Object[this.columnCount];
        cells[this.columnCount - 1] = text;
        return this.row(spanColor, cells);
    }

    public DynamicTableBuilder row(final Object... cells) {
        return this.row(Color.NONE, cells);
    }

    public DynamicTableBuilder row(final Color rowColor, final Object... cells) {
        this.rows.add(new Row(cells, rowColor));
        return this;
    }

    public DynamicTableBuilder rows(final Iterable<? extends Object[]> data) {
        for (Object[] cells : data) {
            this.rows.add(new Row(cells, Color.NONE));
        }
        return this;
    }

    public DynamicTableBuilder align(final Align alignment) {
        this.align = alignment;
        return this;
    }

    public DynamicTableBuilder footer(final String footerText) {
        return this.footer(Color.NONE, footerText);
    }

    public DynamicTableBuilder footer(final Color footerColor, final String footerText) {
        this.footer = footerText == null ? null : new Caption(footerText, footerColor);
        return this;
    }

    public DynamicTableBuilder color(final Color tableColor) {
        this.color = tableColor;
        return this;
    }

    public String build() {
        return TableRenderer.render(new TableSpec(this.title, this.headers, this.columnCount,
                this.rows, this.footer, this.align, this.color));
    }

    private void requireColumnsNotDeclared() {
        if (this.columnCount > 0) {
            throw new IllegalStateException("Table columns are already declared and cannot be redeclared");
        }
    }

}
