package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
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
class ClickhouseExecutorTest {

    private JdbcTemplate mockTemplate;
    private ClickhouseExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        mockTemplate = mock(JdbcTemplate.class);
        executor = new ClickhouseExecutor(null);
        Field templateField = AbstractSqlExecutor.class.getDeclaredField("template");
        templateField.setAccessible(true);
        templateField.set(executor, mockTemplate);
    }

    @Nested
    class Constructor {

        @Test
        void createsExecutorWithNullDataSource() {
            ClickhouseExecutor nullExecutor = new ClickhouseExecutor(null);
            assertNotNull(nullExecutor);
        }

        @Test
        void createsExecutorWithValidDataSource() {
            DataSource ds = mock(DataSource.class);
            ClickhouseExecutor validExecutor = new ClickhouseExecutor(ds);
            assertNotNull(validExecutor);
        }
    }

    @Nested
    class Truncate {

        @Test
        void truncatesAllTablesFromDatabase() {
            when(mockTemplate.queryForObject(
                    eq("SELECT currentDatabase()"), eq(String.class)))
                    .thenReturn("testdb");

            String expectedQuery = "SHOW TABLES FROM testdb WHERE name != 'flyway_schema_history' "
                    + "AND engine != 'MaterializedView' AND name IN (SELECT name FROM system.tables "
                    + "WHERE total_rows > 0 OR total_rows = NULL);";
            when(mockTemplate.queryForList(eq(expectedQuery), eq(String.class)))
                    .thenReturn(List.of("users", "orders"));

            executor.truncate();

            verify(mockTemplate).execute("TRUNCATE TABLE testdb.users");
            verify(mockTemplate).execute("TRUNCATE TABLE testdb.orders");
        }

        @Test
        void doesNotTruncateWhenNoTables() {
            when(mockTemplate.queryForObject(
                    eq("SELECT currentDatabase()"), eq(String.class)))
                    .thenReturn("emptydb");

            String expectedQuery = "SHOW TABLES FROM emptydb WHERE name != 'flyway_schema_history' "
                    + "AND engine != 'MaterializedView' AND name IN (SELECT name FROM system.tables "
                    + "WHERE total_rows > 0 OR total_rows = NULL);";
            when(mockTemplate.queryForList(eq(expectedQuery), eq(String.class)))
                    .thenReturn(List.of());

            executor.truncate();

            verify(mockTemplate, never()).execute(anyString());
        }

        @Test
        void truncatesSingleTable() {
            when(mockTemplate.queryForObject(
                    eq("SELECT currentDatabase()"), eq(String.class)))
                    .thenReturn("mydb");

            String expectedQuery = "SHOW TABLES FROM mydb WHERE name != 'flyway_schema_history' "
                    + "AND engine != 'MaterializedView' AND name IN (SELECT name FROM system.tables "
                    + "WHERE total_rows > 0 OR total_rows = NULL);";
            when(mockTemplate.queryForList(eq(expectedQuery), eq(String.class)))
                    .thenReturn(List.of("single_table"));

            executor.truncate();

            verify(mockTemplate, times(1)).execute("TRUNCATE TABLE mydb.single_table");
        }

        @Test
        void queriesCurrentDatabaseName() {
            when(mockTemplate.queryForObject(
                    eq("SELECT currentDatabase()"), eq(String.class)))
                    .thenReturn("default");
            when(mockTemplate.queryForList(anyString(), eq(String.class)))
                    .thenReturn(List.of());

            executor.truncate();

            verify(mockTemplate).queryForObject("SELECT currentDatabase()", String.class);
        }
    }
}
