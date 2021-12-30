package com.knubisoft.e2e.testing.framework.db.source;

import java.util.List;

public interface Source {
    String QUERY_DELIMITER = ";;";

    List<String> getQueries();
}
