package com.knubisoft.testlum.testing.framework.db.sql.executor.impl;

import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;

import javax.sql.DataSource;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class ClickhouseExecutor extends AbstractSqlExecutor {

    private static final String TRUNCATE_TABLE = "TRUNCATE TABLE %s.%s";
    private static final String SELECT_TABLE_NAMES = "SHOW TABLES FROM %s WHERE name != 'flyway_schema_history'";
    private static final String SELECT_DATABASE_NAME = "SELECT currentDatabase()";

    public ClickhouseExecutor(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void truncate() {
        String databaseName = template.queryForObject(SELECT_DATABASE_NAME, String.class);
        List<String> tables = template.queryForList(format(SELECT_TABLE_NAMES, databaseName), String.class);
        for (String table : tables) {
            requireNonNull(template).execute(format(TRUNCATE_TABLE, databaseName, table));
        }
    }
}
