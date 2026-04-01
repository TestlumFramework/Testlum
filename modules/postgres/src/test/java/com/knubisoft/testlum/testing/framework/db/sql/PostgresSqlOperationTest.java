package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PostgresSqlOperationTest {

    @Nested
    class Constructor {

        @Test
        void createsOperationWithEmptyDataSourceMap() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            PostgresSqlOperation operation = new PostgresSqlOperation(dataSourceMap);

            assertNotNull(operation);
        }

        @Test
        void createsOperationWithSingleDataSource() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            dataSourceMap.put(new AliasEnv("pg1", "env1"), mock(DataSource.class));

            PostgresSqlOperation operation = new PostgresSqlOperation(dataSourceMap);

            assertNotNull(operation);
        }

        @Test
        void createsOperationWithMultipleDataSources() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            dataSourceMap.put(new AliasEnv("pg1", "env1"), mock(DataSource.class));
            dataSourceMap.put(new AliasEnv("pg2", "env1"), mock(DataSource.class));

            PostgresSqlOperation operation = new PostgresSqlOperation(dataSourceMap);

            assertNotNull(operation);
        }
    }

    @Nested
    class CreateExecutor {

        @Test
        void createsPostgresExecutorInstance() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            PostgresSqlOperation operation = new PostgresSqlOperation(dataSourceMap);
            DataSource ds = mock(DataSource.class);

            AbstractSqlExecutor executor = operation.createExecutor(ds);

            assertNotNull(executor);
            assertInstanceOf(PostgresExecutor.class, executor);
        }

        @Test
        void createsExecutorWithNullDataSource() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            PostgresSqlOperation operation = new PostgresSqlOperation(dataSourceMap);

            AbstractSqlExecutor executor = operation.createExecutor(null);

            assertNotNull(executor);
            assertInstanceOf(PostgresExecutor.class, executor);
        }
    }
}
