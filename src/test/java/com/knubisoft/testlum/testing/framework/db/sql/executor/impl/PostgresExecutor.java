package com.knubisoft.testlum.testing.framework.db.sql.executor.impl;


import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

public class PostgresExecutor extends AbstractSqlExecutor {

    private static final String ALTER_TABLE_DISABLE_TRIGGER_ALL = "ALTER TABLE \"%s\" DISABLE TRIGGER ALL";
    private static final String ALTER_TABLE_ENABLE_TRIGGER_ALL = "ALTER TABLE \"%s\" ENABLE TRIGGER ALL";
    private static final String TRUNCATE_TABLE_AND_RESTART_SEQUENCE = "TRUNCATE \"%s\" RESTART IDENTITY CASCADE";
    private static final String SELECT_POSTGRES_TABLE_NAMES = "SELECT tablename FROM pg_tables "
            + "WHERE schemaname = '%s' AND tablename != 'flyway_schema_history';";

    private static final List<String> TRUNCATE_QUERIES = Arrays.asList(
            ALTER_TABLE_DISABLE_TRIGGER_ALL,
            TRUNCATE_TABLE_AND_RESTART_SEQUENCE,
            ALTER_TABLE_ENABLE_TRIGGER_ALL);

    public PostgresExecutor(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void truncate() {
        super.defaultTruncate(SELECT_POSTGRES_TABLE_NAMES, TRUNCATE_QUERIES);
    }
}
