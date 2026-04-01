package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class PostgresExecutorTest {

    private JdbcTemplate mockTemplate;
    private PostgresExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        mockTemplate = mock(JdbcTemplate.class);
        executor = new PostgresExecutor(null);
        Field templateField = AbstractSqlExecutor.class.getDeclaredField("template");
        templateField.setAccessible(true);
        templateField.set(executor, mockTemplate);
    }

    @Nested
    class Constructor {

        @Test
        void createsExecutorWithNullDataSource() {
            PostgresExecutor nullExecutor = new PostgresExecutor(null);
            assertNotNull(nullExecutor);
        }

        @Test
        void createsExecutorWithValidDataSource() {
            DataSource ds = mock(DataSource.class);
            PostgresExecutor validExecutor = new PostgresExecutor(ds);
            assertNotNull(validExecutor);
        }
    }

    @Nested
    class Truncate {

        @Test
        void truncatesAllTablesWithTriggerManagement() {
            HikariDataSource hikariDs = mock(HikariDataSource.class);
            when(hikariDs.getSchema()).thenReturn("public");
            when(mockTemplate.getDataSource()).thenReturn(hikariDs);

            String selectQuery = "SELECT tablename FROM pg_tables "
                    + "WHERE schemaname = 'public' AND tablename != 'flyway_schema_history' "
                    + "AND tablename IN (SELECT relname FROM pg_stat_user_tables WHERE n_tup_ins > 0);";
            when(mockTemplate.queryForList(eq(selectQuery), eq(String.class)))
                    .thenReturn(List.of("users", "orders"));

            executor.truncate();

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(mockTemplate, atLeast(6)).execute(captor.capture());

            List<String> executed = captor.getAllValues();
            assertTrue(executed.stream().anyMatch(q -> q.contains("DISABLE TRIGGER ALL")));
            assertTrue(executed.stream().anyMatch(q -> q.contains("RESTART IDENTITY CASCADE")));
            assertTrue(executed.stream().anyMatch(q -> q.contains("ENABLE TRIGGER ALL")));
        }

        @Test
        void doesNotTruncateWhenNoTables() {
            HikariDataSource hikariDs = mock(HikariDataSource.class);
            when(hikariDs.getSchema()).thenReturn("public");
            when(mockTemplate.getDataSource()).thenReturn(hikariDs);

            String selectQuery = "SELECT tablename FROM pg_tables "
                    + "WHERE schemaname = 'public' AND tablename != 'flyway_schema_history' "
                    + "AND tablename IN (SELECT relname FROM pg_stat_user_tables WHERE n_tup_ins > 0);";
            when(mockTemplate.queryForList(eq(selectQuery), eq(String.class)))
                    .thenReturn(List.of());

            executor.truncate();

            verify(mockTemplate, never()).execute(anyString());
        }

        @Test
        void usesSchemaFromDataSource() {
            HikariDataSource hikariDs = mock(HikariDataSource.class);
            when(hikariDs.getSchema()).thenReturn("custom_schema");
            when(mockTemplate.getDataSource()).thenReturn(hikariDs);

            String selectQuery = "SELECT tablename FROM pg_tables "
                    + "WHERE schemaname = 'custom_schema' AND tablename != 'flyway_schema_history' "
                    + "AND tablename IN (SELECT relname FROM pg_stat_user_tables WHERE n_tup_ins > 0);";
            when(mockTemplate.queryForList(eq(selectQuery), eq(String.class)))
                    .thenReturn(List.of());

            executor.truncate();

            verify(mockTemplate).queryForList(eq(selectQuery), eq(String.class));
        }

        @Test
        void executesTriggerDisableBeforeTruncate() {
            HikariDataSource hikariDs = mock(HikariDataSource.class);
            when(hikariDs.getSchema()).thenReturn("public");
            when(mockTemplate.getDataSource()).thenReturn(hikariDs);

            String selectQuery = "SELECT tablename FROM pg_tables "
                    + "WHERE schemaname = 'public' AND tablename != 'flyway_schema_history' "
                    + "AND tablename IN (SELECT relname FROM pg_stat_user_tables WHERE n_tup_ins > 0);";
            when(mockTemplate.queryForList(eq(selectQuery), eq(String.class)))
                    .thenReturn(List.of("test_table"));

            executor.truncate();

            verify(mockTemplate).execute("ALTER TABLE \"test_table\" DISABLE TRIGGER ALL");
            verify(mockTemplate).execute("TRUNCATE \"test_table\" RESTART IDENTITY CASCADE");
            verify(mockTemplate).execute("ALTER TABLE \"test_table\" ENABLE TRIGGER ALL");
        }
    }
}
