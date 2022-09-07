package com.knubisoft.cott.testing.framework.db.sql.executor.impl;


import com.knubisoft.cott.testing.framework.db.sql.executor.AbstractSqlExecutor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class MySqlExecutor extends AbstractSqlExecutor {

    private static final String DISABLE_FOREIGN_KEY_CHECKS = "SET FOREIGN_KEY_CHECKS=0;";
    private static final String ENABLE_FOREIGN_KEYS_CHECKS = "SET FOREIGN_KEY_CHECKS=1;";
    private static final String TRUNCATE_TABLE = "TRUNCATE TABLE `%s`";
    private static final String SELECT_MYSQL_TABLE_NAMES = "SELECT TABLE_NAME FROM information_schema.tables "
            + "WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME != 'flyway_schema_history';";

    private static final List<String> TRUNCATE_QUERIES = Arrays.asList(
            DISABLE_FOREIGN_KEY_CHECKS,
            TRUNCATE_TABLE,
            ENABLE_FOREIGN_KEYS_CHECKS);

    public MySqlExecutor(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void truncate() {
        super.defaultTruncate(SELECT_MYSQL_TABLE_NAMES, TRUNCATE_QUERIES);
    }
}
