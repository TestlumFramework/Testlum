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
class ClickhouseOperationTest {

    @Nested
    class Constructor {

        @Test
        void createsOperationWithEmptyDataSourceMap() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            ClickhouseOperation operation = new ClickhouseOperation(dataSourceMap);

            assertNotNull(operation);
        }

        @Test
        void createsOperationWithSingleDataSource() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            DataSource ds = mock(DataSource.class);
            dataSourceMap.put(new AliasEnv("ch1", "env1"), ds);

            ClickhouseOperation operation = new ClickhouseOperation(dataSourceMap);

            assertNotNull(operation);
        }

        @Test
        void createsOperationWithMultipleDataSources() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            dataSourceMap.put(new AliasEnv("ch1", "env1"), mock(DataSource.class));
            dataSourceMap.put(new AliasEnv("ch2", "env1"), mock(DataSource.class));

            ClickhouseOperation operation = new ClickhouseOperation(dataSourceMap);

            assertNotNull(operation);
        }
    }

    @Nested
    class CreateExecutor {

        @Test
        void createsClickhouseExecutorInstance() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            ClickhouseOperation operation = new ClickhouseOperation(dataSourceMap);
            DataSource ds = mock(DataSource.class);

            AbstractSqlExecutor executor = operation.createExecutor(ds);

            assertNotNull(executor);
            assertInstanceOf(ClickhouseExecutor.class, executor);
        }

        @Test
        void createsExecutorWithNullDataSource() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            ClickhouseOperation operation = new ClickhouseOperation(dataSourceMap);

            AbstractSqlExecutor executor = operation.createExecutor(null);

            assertNotNull(executor);
            assertInstanceOf(ClickhouseExecutor.class, executor);
        }
    }
}
