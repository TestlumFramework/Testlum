package com.knubisoft.cott.testing.framework.db.sql.executor.impl;


import com.knubisoft.cott.testing.framework.db.sql.executor.AbstractSqlExecutor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Slf4j
public class MySqlExecutor extends AbstractSqlExecutor {

    private static final String ALTER_TABLE_DISABLE_TRIGGER_ALL = "ALTER TABLE %s DISABLE KEYS";
    private static final String ALTER_TABLE_ENABLE_TRIGGER_ALL = "ALTER TABLE %s ENABLE KEYS";
    private static final String TRUNCATE_TABLE = "TRUNCATE TABLE %s";
    private static final String SELECT_MYSQL_TABLE_NAMES = "SELECT TABLE_NAME FROM information_schema.tables "
            + "WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME != 'flyway_schema_history';";

    private static final List<String> TRUNCATE_QUERIES = Arrays.asList(
            ALTER_TABLE_DISABLE_TRIGGER_ALL,
            TRUNCATE_TABLE,
            ALTER_TABLE_ENABLE_TRIGGER_ALL);

    public MySqlExecutor(final DataSource dataSource) {
        super(dataSource);
    }

    public void truncate(final String schemaName) {
        List<String> tables = template.queryForList(format(SELECT_MYSQL_TABLE_NAMES, schemaName), String.class);
        for (String table : tables) {
            for (String query : TRUNCATE_QUERIES) {
                requireNonNull(template).execute(format(query, table));
            }
        }
    }

    @Override
    public void truncate() {
        List<String> tables = template.queryForList(SELECT_MYSQL_TABLE_NAMES, String.class);
        for (String table : tables) {
            for (String query : TRUNCATE_QUERIES) {
                requireNonNull(template).execute(format(query, table));
            }
        }
    }
}
