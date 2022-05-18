package com.knubisoft.e2e.testing.framework.db.sql.executor.impl;


import com.knubisoft.e2e.testing.framework.db.sql.executor.AbstractSqlExecutor;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class PostgresExecutor extends AbstractSqlExecutor {

    private static final String PK_NAME = "id";
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
    protected List<Number> getAffectedKeys(final List<Map<String, Object>> keyList) {
        return keyList.stream().map(e -> e.get(PK_NAME))
                .filter(Objects::nonNull)
                .map(v -> (Number) v)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public void truncate() {
        final String schemaName = requireNonNull(template.getDataSource()).getConnection().getSchema();
        List<String> tables = template.queryForList(format(SELECT_POSTGRES_TABLE_NAMES, schemaName), String.class);
        for (String table : tables) {
            for (String query : TRUNCATE_QUERIES) {
                requireNonNull(template).execute(format(query, table));
            }
        }
    }
}
