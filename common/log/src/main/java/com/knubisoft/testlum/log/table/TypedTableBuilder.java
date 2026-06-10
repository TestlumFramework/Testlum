package com.knubisoft.testlum.log.table;

import com.knubisoft.testlum.log.Color;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class TypedTableBuilder<T> {

    private final String title;
    private final Map<String, Function<? super T, ?>> columns = new LinkedHashMap<>();
    private final List<T> data = new ArrayList<>();
    private Align align = Align.LEFT;
    private String footer;
    private Color color = Color.NONE;

    TypedTableBuilder(final String title) {
        this.title = title;
    }

    public TypedTableBuilder<T> column(final String header, final Function<? super T, ?> extractor) {
        this.columns.put(header, extractor);
        return this;
    }

    public TypedTableBuilder<T> rows(final Iterable<? extends T> source) {
        for (T item : source) {
            this.data.add(item);
        }
        return this;
    }

    public TypedTableBuilder<T> align(final Align alignment) {
        this.align = alignment;
        return this;
    }

    public TypedTableBuilder<T> footer(final String footerText) {
        this.footer = footerText;
        return this;
    }

    public TypedTableBuilder<T> color(final Color tableColor) {
        this.color = tableColor;
        return this;
    }

    public String build() {
        String[] headers = this.columns.keySet().toArray(new String[0]);
        List<Row> materialized = this.materializeRows(headers.length);
        Caption titleCaption = this.title == null ? null : new Caption(this.title, Color.NONE);
        Caption footerCaption = this.footer == null ? null : new Caption(this.footer, Color.NONE);
        return TableRenderer.render(new TableSpec(titleCaption, headers, materialized,
                footerCaption, this.align, this.color));
    }

    private List<Row> materializeRows(final int columnCount) {
        List<Row> materialized = new ArrayList<>(this.data.size());
        for (T item : this.data) {
            Object[] cells = new Object[columnCount];
            int i = 0;
            for (Function<? super T, ?> extractor : this.columns.values()) {
                cells[i++] = extractor.apply(item);
            }
            materialized.add(new Row(cells, Color.NONE));
        }
        return materialized;
    }
}
