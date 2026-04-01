package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OracleExecutorTest {

    private JdbcTemplate mockTemplate;
    private OracleExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        mockTemplate = mock(JdbcTemplate.class);
        executor = new OracleExecutor(null);
        Field templateField = AbstractSqlExecutor.class.getDeclaredField("template");
        templateField.setAccessible(true);
        templateField.set(executor, mockTemplate);
    }

    @Nested
    class Constructor {

        @Test
        void createsExecutorWithNullDataSource() {
            OracleExecutor nullExecutor = new OracleExecutor(null);
            assertNotNull(nullExecutor);
        }

        @Test
        void createsExecutorWithValidDataSource() {
            DataSource ds = mock(DataSource.class);
            OracleExecutor validExecutor = new OracleExecutor(ds);
            assertNotNull(validExecutor);
        }
    }

    @Nested
    class Truncate {

        @Test
        void executesDisableConstraintsThenTruncateThenEnable() {
            String disableQuery = "SELECT x1.table_name, x1.constraint_name "
                    + "FROM user_tables t1, user_constraints x1 "
                    + "WHERE t1.table_name = x1.table_name AND x1.constraint_type <> 'C' "
                    + "ORDER BY x1.r_constraint_name NULLS LAST";
            String selectTablesQuery = "SELECT table_name FROM user_tables "
                    + "WHERE table_name IN (SELECT table_name FROM user_tab_columns "
                    + "GROUP BY table_name HAVING COUNT(*) > 0)";
            String enableQuery = "SELECT x1.table_name, x1.constraint_name "
                    + "FROM user_tables t1, user_constraints x1 "
                    + "WHERE t1.table_name = x1.table_name AND x1.constraint_type <> 'C' "
                    + "ORDER BY x1.r_constraint_name NULLS FIRST";

            Map<String, Object> constraintRow = Map.of(
                    "table_name", "USERS",
                    "constraint_name", "FK_USERS_ORDERS"
            );
            Map<String, Object> tableRow = Map.of(
                    "table_name", "USERS"
            );

            when(mockTemplate.queryForList(disableQuery))
                    .thenReturn(List.of(constraintRow));
            when(mockTemplate.queryForList(selectTablesQuery))
                    .thenReturn(List.of(tableRow));
            when(mockTemplate.queryForList(enableQuery))
                    .thenReturn(List.of(constraintRow));

            executor.truncate();

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(mockTemplate, atLeast(3)).execute(captor.capture());

            List<String> executedQueries = captor.getAllValues();
            assertTrue(executedQueries.stream()
                    .anyMatch(q -> q.contains("DISABLE CONSTRAINT")));
            assertTrue(executedQueries.stream()
                    .anyMatch(q -> q.startsWith("TRUNCATE TABLE")));
            assertTrue(executedQueries.stream()
                    .anyMatch(q -> q.contains("ENABLE CONSTRAINT")));
        }

        @Test
        void doesNotExecuteWhenNoTablesOrConstraints() {
            when(mockTemplate.queryForList(anyString()))
                    .thenReturn(List.of());

            executor.truncate();

            verify(mockTemplate, never()).execute(anyString());
        }

        @Test
        void handlesMultipleTablesWithConstraints() {
            Map<String, Object> constraint1 = Map.of(
                    "table_name", "ORDERS",
                    "constraint_name", "FK_ORDERS_USERS"
            );
            Map<String, Object> constraint2 = Map.of(
                    "table_name", "ITEMS",
                    "constraint_name", "FK_ITEMS_ORDERS"
            );

            when(mockTemplate.queryForList(anyString()))
                    .thenReturn(List.of(constraint1, constraint2));

            executor.truncate();

            verify(mockTemplate, atLeast(6)).execute(anyString());
        }

        @Test
        void formatsDisableConstraintQueryCorrectly() {
            Map<String, Object> row = Map.of(
                    "table_name", "TEST_TABLE",
                    "constraint_name", "FK_TEST"
            );

            String disableQuery = "SELECT x1.table_name, x1.constraint_name "
                    + "FROM user_tables t1, user_constraints x1 "
                    + "WHERE t1.table_name = x1.table_name AND x1.constraint_type <> 'C' "
                    + "ORDER BY x1.r_constraint_name NULLS LAST";

            when(mockTemplate.queryForList(disableQuery)).thenReturn(List.of(row));
            when(mockTemplate.queryForList(argThat(q -> !q.equals(disableQuery))))
                    .thenReturn(List.of());

            executor.truncate();

            verify(mockTemplate).execute("ALTER TABLE TEST_TABLE DISABLE CONSTRAINT FK_TEST");
        }
    }
}
