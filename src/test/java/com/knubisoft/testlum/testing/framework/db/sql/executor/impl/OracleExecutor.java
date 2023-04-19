package com.knubisoft.testlum.testing.framework.db.sql.executor.impl;

import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class OracleExecutor extends AbstractSqlExecutor {

    private static final String SELECT_ORACLE_TABLE_NAMES_AND_CONSTRAINTS_FOR_DISABLE =
            "SELECT x1.table_name, x1.constraint_name FROM user_tables t1, user_constraints x1 "
                    + "WHERE t1.table_name = x1.table_name ORDER BY x1.r_constraint_name NULLS LAST";
    private static final String SELECT_ORACLE_TABLE_NAMES_AND_CONSTRAINTS_FOR_ENABLE =
            "SELECT x1.table_name, x1.constraint_name FROM user_tables t1, user_constraints x1 "
                    + "WHERE t1.table_name = x1.table_name ORDER BY x1.r_constraint_name NULLS FIRST";
    private static final String SELECT_ORACLE_TABLE_NAMES = "SELECT table_name FROM user_tables";
    private static final String DISABLE_CONSTRAINT = "ALTER TABLE %s DISABLE CONSTRAINT %s";
    private static final String TRUNCATE_TABLE = "TRUNCATE TABLE %1$s";
    private static final String ENABLE_CONSTRAINT = "ALTER TABLE %s ENABLE CONSTRAINT %s";

    private static final String TABLE_NAME = "table_name";
    private static final String CONSTRAINT_NAME = "constraint_name";

    private static final Map<String, String> SELECT_TO_TRUNCATE_QUERIES = new LinkedHashMap<>();

    static {
        SELECT_TO_TRUNCATE_QUERIES.put(SELECT_ORACLE_TABLE_NAMES_AND_CONSTRAINTS_FOR_DISABLE, DISABLE_CONSTRAINT);
        SELECT_TO_TRUNCATE_QUERIES.put(SELECT_ORACLE_TABLE_NAMES, TRUNCATE_TABLE);
        SELECT_TO_TRUNCATE_QUERIES.put(SELECT_ORACLE_TABLE_NAMES_AND_CONSTRAINTS_FOR_ENABLE, ENABLE_CONSTRAINT);
    }

    public OracleExecutor(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void truncate() {
        SELECT_TO_TRUNCATE_QUERIES.forEach((selectQuery, tableQuery) -> {
            List<Map<String, Object>> tableList = requireNonNull(template).queryForList(selectQuery);
            tableList.forEach(row -> {
                String tableName = (String) row.get(TABLE_NAME);
                String constraint = (String) row.get(CONSTRAINT_NAME);
                template.execute(format(tableQuery, tableName, constraint));
            });
        });
    }
}
