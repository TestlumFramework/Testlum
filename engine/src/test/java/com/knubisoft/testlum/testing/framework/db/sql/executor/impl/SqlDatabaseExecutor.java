package com.knubisoft.testlum.testing.framework.db.sql.executor.impl;

import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;

import javax.sql.DataSource;

public class SqlDatabaseExecutor extends AbstractSqlExecutor {

    public SqlDatabaseExecutor(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void truncate() {

    }
}
