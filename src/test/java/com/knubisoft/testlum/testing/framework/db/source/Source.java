package com.knubisoft.testlum.testing.framework.db.source;

import java.util.List;

public interface Source {
    String QUERY_DELIMITER = ";;";

    List<String> getQueries();
}
