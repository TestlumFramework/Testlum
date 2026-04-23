package com.knubisoft.testlum.log.table;

public final class TableBuilder {

    private TableBuilder() {
    }

    public static DynamicTableBuilder grid() {
        return new DynamicTableBuilder(null);
    }

    public static DynamicTableBuilder grid(final String title) {
        return new DynamicTableBuilder(title);
    }

    public static <T> TypedTableBuilder<T> grid(final Class<T> rowType) {
        return new TypedTableBuilder<>(null);
    }

    public static <T> TypedTableBuilder<T> grid(final String title, final Class<T> rowType) {
        return new TypedTableBuilder<>(title);
    }
}
