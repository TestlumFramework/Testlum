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
class MySqlOperationTest {

    @Nested
    class Constructor {

        @Test
        void createsOperationWithEmptyDataSourceMap() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            MySqlOperation operation = new MySqlOperation(dataSourceMap);

            assertNotNull(operation);
        }

        @Test
        void createsOperationWithSingleDataSource() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            dataSourceMap.put(new AliasEnv("mysql1", "env1"), mock(DataSource.class));

            MySqlOperation operation = new MySqlOperation(dataSourceMap);

            assertNotNull(operation);
        }

        @Test
        void createsOperationWithNullMap() {
            assertThrows(NullPointerException.class, () -> new MySqlOperation(null));
        }
    }

    @Nested
    class CreateExecutor {

        @Test
        void createsMySqlExecutorInstance() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            MySqlOperation operation = new MySqlOperation(dataSourceMap);
            DataSource ds = mock(DataSource.class);

            AbstractSqlExecutor executor = operation.createExecutor(ds);

            assertNotNull(executor);
            assertInstanceOf(MySqlExecutor.class, executor);
        }

        @Test
        void createsExecutorWithNullDataSource() {
            Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
            MySqlOperation operation = new MySqlOperation(dataSourceMap);

            AbstractSqlExecutor executor = operation.createExecutor(null);

            assertNotNull(executor);
            assertInstanceOf(MySqlExecutor.class, executor);
        }
    }
}
