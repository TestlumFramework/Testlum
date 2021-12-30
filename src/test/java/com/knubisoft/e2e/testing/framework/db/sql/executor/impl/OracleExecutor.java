package com.knubisoft.e2e.testing.framework.db.sql.executor.impl;

import com.knubisoft.e2e.testing.framework.db.sql.executor.AbstractSqlExecutor;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    protected List<Number> getAffectedKeys(final List<Map<String, Object>> keyList) {
        return keyList.stream().map(e -> e.get("ID"))
                .filter(Objects::nonNull)
                .map(v -> (Number) v)
                .collect(Collectors.toList());
    }

    @Override
    public void truncate() {
        for (String query : TRUNCATE_QUERIES) {
            requireNonNull(template).execute(query);
        }
    }
}
