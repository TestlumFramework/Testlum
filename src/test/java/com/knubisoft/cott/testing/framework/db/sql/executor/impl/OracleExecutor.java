package com.knubisoft.cott.testing.framework.db.sql.executor.impl;

import com.knubisoft.cott.testing.framework.db.sql.executor.AbstractSqlExecutor;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class OracleExecutor extends AbstractSqlExecutor {

    private static final String DISABLE_FOREIGN_KEYS = "CALL DISABLE_FOREIGN_KEYS()";
    private static final String DISABLE_PRIMARY_KEYS = "CALL DISABLE_PRIMARY_KEYS()";
    private static final String TRUNCATE_TABLES = "CALL TRUNCATE_TABLES()";
    private static final String ENABLE_PRIMARY_KEYS = "CALL ENABLE_PRIMARY_KEYS()";
    private static final String ENABLE_FOREIGN_KEYS = "CALL ENABLE_FOREIGN_KEYS()";

    private static final List<String> TRUNCATE_QUERIES = Arrays.asList(
            DISABLE_FOREIGN_KEYS,
            DISABLE_PRIMARY_KEYS,
            TRUNCATE_TABLES,
            ENABLE_PRIMARY_KEYS,
            ENABLE_FOREIGN_KEYS);

    public OracleExecutor(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void truncate() {
        for (String query : TRUNCATE_QUERIES) {
            requireNonNull(template).execute(query);
        }
    }
}
