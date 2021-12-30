package com.knubisoft.e2e.testing.framework.db.source;

import java.util.Arrays;
import java.util.List;

public class StringsSource implements Source {

    private final ListSource listSource;

    public StringsSource(final String queries) {
        this.listSource = new ListSource(Arrays.asList(queries.split(QUERY_DELIMITER)));
    }

    @Override
    public List<String> getQueries() {
        return listSource.getQueries();
    }
}
