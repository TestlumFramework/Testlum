package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MySqlExecutorTest {

    private JdbcTemplate mockTemplate;
    private MySqlExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        mockTemplate = mock(JdbcTemplate.class);
        executor = new MySqlExecutor(null);
        Field templateField = AbstractSqlExecutor.class.getDeclaredField("template");
        templateField.setAccessible(true);
        templateField.set(executor, mockTemplate);
    }

    @Nested
    class Constructor {

        @Test
        void createsExecutorWithNullDataSource() {
            MySqlExecutor nullExecutor = new MySqlExecutor(null);
            assertNotNull(nullExecutor);
        }

        @Test
        void createsExecutorWithValidDataSource() {
            DataSource ds = mock(DataSource.class);
            MySqlExecutor validExecutor = new MySqlExecutor(ds);
            assertNotNull(validExecutor);
        }
    }

    @Nested
    class Truncate {

        @Test
        void truncatesAllTablesWithForeignKeyManagement() {
            HikariDataSource hikariDs = mock(HikariDataSource.class);
            when(hikariDs.getSchema()).thenReturn("test_schema");
            when(mockTemplate.getDataSource()).thenReturn(hikariDs);

            String selectQuery = "SELECT TABLE_NAME FROM information_schema.tables "
                    + "WHERE TABLE_SCHEMA = 'test_schema' AND TABLE_NAME != 'flyway_schema_history';";
            when(mockTemplate.queryForList(eq(selectQuery), eq(String.class)))
                    .thenReturn(List.of("users", "orders"));

            executor.truncate();

            verify(mockTemplate, times(2)).execute("SET FOREIGN_KEY_CHECKS=0;");
            verify(mockTemplate).execute("TRUNCATE TABLE `users`");
            verify(mockTemplate).execute("TRUNCATE TABLE `orders`");
            verify(mockTemplate, times(2)).execute("SET FOREIGN_KEY_CHECKS=1;");
        }

        @Test
        void doesNotTruncateWhenNoTables() {
            HikariDataSource hikariDs = mock(HikariDataSource.class);
            when(hikariDs.getSchema()).thenReturn("empty_schema");
            when(mockTemplate.getDataSource()).thenReturn(hikariDs);

            String selectQuery = "SELECT TABLE_NAME FROM information_schema.tables "
                    + "WHERE TABLE_SCHEMA = 'empty_schema' AND TABLE_NAME != 'flyway_schema_history';";
            when(mockTemplate.queryForList(eq(selectQuery), eq(String.class)))
                    .thenReturn(List.of());

            executor.truncate();

            verify(mockTemplate, never()).execute(anyString());
        }

        @Test
        void usesSchemaFromDataSource() {
            HikariDataSource hikariDs = mock(HikariDataSource.class);
            when(hikariDs.getSchema()).thenReturn("my_database");
            when(mockTemplate.getDataSource()).thenReturn(hikariDs);

            String selectQuery = "SELECT TABLE_NAME FROM information_schema.tables "
                    + "WHERE TABLE_SCHEMA = 'my_database' AND TABLE_NAME != 'flyway_schema_history';";
            when(mockTemplate.queryForList(eq(selectQuery), eq(String.class)))
                    .thenReturn(List.of());

            executor.truncate();

            verify(mockTemplate).queryForList(eq(selectQuery), eq(String.class));
        }
    }
}
