package com.knubisoft.testlum.testing.framework.db.sql.executor.impl;

import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;

import javax.sql.DataSource;

public class SqlDatabaseExecutor extends AbstractSqlExecutor {

    public SqlDatabaseExecutor(final DataSource dataSource) {
        super(dataSource);
    }

    //todo
    @Override
    public void truncate() {
        // TODO: Implement database truncation logic.
        // Option 1: Automatically detect the database type (e.g., MariaDB, MSSQL, MonetDB)
        // using DatabaseMetaData and delegate the execution to a corresponding Executor class
        // such as MySqlExecutor or PostgresExecutor.
        //
        // Option 2: Support a custom <truncate> section in the XML configuration.
        // If <truncate><step>...</step></truncate> steps are defined,
//        <truncate>
//          <step>SET FOREIGN_KEY_CHECKS=0;</step>
//          <step>TRUNCATE TABLE users;</step>
//          <step>TRUNCATE TABLE orders;</step>
//          <step>SET FOREIGN_KEY_CHECKS=1;</step>
//        </truncate>
        // execute each step sequentially as raw SQL statements.
        //
        // Priority: if the <truncate> section exists in the config, execute those steps.
        // Otherwise, fall back to auto-detection and built-in logic.
    }
}
