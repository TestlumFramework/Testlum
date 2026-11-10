package com.testlum.testing.framework.db.sql.executor;

import com.testlum.testing.framework.db.AbstractStorageOperation.QueryResult;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
abstract class AbstractSqlExecutorTest {

    private TestSqlExecutor createExecutorWith(final JdbcTemplate mockTemplate) {
        TestSqlExecutor executor = new TestSqlExecutor(null);
        try {
            Field templateField = AbstractSqlExecutor.class.getDeclaredField("template");
            templateField.setAccessible(true);
            templateField.set(executor, mockTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return executor;
    }

    @Nested
    class NullDataSource {
        @Test
        void nullDataSourceCreatesExecutorWithNullTemplate() {
            TestSqlExecutor executor = new TestSqlExecutor(null);
            assertNull(executor.template);
        }
    }

    @Nested
    class ValidDataSource {
        @Test
        void nonNullDataSourceCreatesJdbcTemplate() {
            DataSource ds = mock(DataSource.class);
            TestSqlExecutor executor = new TestSqlExecutor(ds);
            assertNotNull(executor.template);
        }
    }

    @Nested
    class ExecuteQueries {
        @Test
        void insertQueryExecutedAsDML() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            when(mockTemplate.update(any(PreparedStatementCreator.class))).thenReturn(1);
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(List.of("INSERT INTO t VALUES(1)"));
            assertEquals(1, results.size());
            assertNotNull(results.get(0).getContent());
            @SuppressWarnings("unchecked")
            Map<String, Integer> content = (Map<String, Integer>) results.get(0).getContent();
            assertEquals(1, content.get("count"));
        }

        @Test
        void updateQueryExecutedAsDML() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            when(mockTemplate.update(any(PreparedStatementCreator.class))).thenReturn(3);
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(List.of("UPDATE t SET x=1"));
            @SuppressWarnings("unchecked")
            Map<String, Integer> content = (Map<String, Integer>) results.get(0).getContent();
            assertEquals(3, content.get("count"));
        }

        @Test
        void deleteQueryExecutedAsDML() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            when(mockTemplate.update(any(PreparedStatementCreator.class))).thenReturn(5);
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(List.of("DELETE FROM t WHERE id=1"));
            @SuppressWarnings("unchecked")
            Map<String, Integer> content = (Map<String, Integer>) results.get(0).getContent();
            assertEquals(5, content.get("count"));
        }

        @Test
        void createQueryExecutedAsDDL() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            doNothing().when(mockTemplate).execute(eq("CREATE TABLE t (id INT)"));
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(List.of("CREATE TABLE t (id INT)"));
            assertEquals("Query completed successfully", results.get(0).getContent());
        }

        @Test
        void alterQueryExecutedAsDDL() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            doNothing().when(mockTemplate).execute(eq("ALTER TABLE t ADD COLUMN x INT"));
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(List.of("ALTER TABLE t ADD COLUMN x INT"));
            assertEquals("Query completed successfully", results.get(0).getContent());
        }

        @Test
        void truncateQueryExecutedAsDDL() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            doNothing().when(mockTemplate).execute(eq("TRUNCATE TABLE t"));
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(List.of("TRUNCATE TABLE t"));
            assertEquals("Query completed successfully", results.get(0).getContent());
        }

        @Test
        void dropQueryExecutedAsDDL() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            doNothing().when(mockTemplate).execute(eq("DROP TABLE t"));
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(List.of("DROP TABLE t"));
            assertEquals("Query completed successfully", results.get(0).getContent());
        }

        @Test
        void setQueryExecutedAsDDL() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            doNothing().when(mockTemplate).execute(eq("SET search_path TO public"));
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(List.of("SET search_path TO public"));
            assertEquals("Query completed successfully", results.get(0).getContent());
        }

        @Test
        void selectQueryExecutedAsDQL() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            List<Map<String, Object>> expected = List.of(Map.of("id", 1, "name", "John"));
            when(mockTemplate.queryForList("SELECT * FROM users")).thenReturn(expected);
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(List.of("SELECT * FROM users"));
            assertEquals(expected, results.get(0).getContent());
        }

        @Test
        void multipleQueriesExecutedInOrder() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            when(mockTemplate.update(any(PreparedStatementCreator.class))).thenReturn(1);
            when(mockTemplate.queryForList("SELECT 1")).thenReturn(List.of(Map.of("1", 1)));
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(
                    List.of("INSERT INTO t VALUES(1)", "SELECT 1"));
            assertEquals(2, results.size());
        }

        @Test
        void emptyQueryListReturnsEmptyResults() {
            TestSqlExecutor executor = createExecutorWith(mock(JdbcTemplate.class));
            List<QueryResult<Object>> results = executor.executeQueries(Collections.emptyList());
            assertTrue(results.isEmpty());
        }

        @Test
        void queryWithLeadingSpaceClassifiedCorrectly() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            when(mockTemplate.update(any(PreparedStatementCreator.class))).thenReturn(1);
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(List.of(" INSERT INTO t VALUES(1)"));
            @SuppressWarnings("unchecked")
            Map<String, Integer> content = (Map<String, Integer>) results.get(0).getContent();
            assertEquals(1, content.get("count"));
        }

        @Test
        void queryWithNewlinesNormalized() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            when(mockTemplate.queryForList("SELECT *FROM users")).thenReturn(List.of());
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(List.of("SELECT *\nFROM users"));
            assertEquals("SELECT *FROM users", results.get(0).getQuery());
        }

        @Test
        void sqlExceptionRethrownWithDatabaseReason() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            when(mockTemplate.update(any(PreparedStatementCreator.class)))
                    .thenThrow(new InvalidDataAccessResourceUsageException("Bad SQL. Position: 10"));
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> executor.executeQueries(List.of("INSERT INTO bad_table VALUES(1)")));
            assertEquals("Bad SQL.", ex.getMessage());
        }

        @Test
        void lowercaseQueryClassifiedCorrectly() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            when(mockTemplate.update(any(PreparedStatementCreator.class))).thenReturn(1);
            TestSqlExecutor executor = createExecutorWith(mockTemplate);

            List<QueryResult<Object>> results = executor.executeQueries(List.of("insert into t values(1)"));
            @SuppressWarnings("unchecked")
            Map<String, Integer> content = (Map<String, Integer>) results.get(0).getContent();
            assertEquals(1, content.get("count"));
        }
    }

    @Nested
    class LogSqlExceptionTest {
        @Test
        void logSqlExceptionWithMessage() {
            TestSqlExecutor executor = new TestSqlExecutor(null);
            Exception ex = new RuntimeException("SQL error at Position: 15");
            executor.logSqlException(ex, "SELECT * FROM t WHERE invalid");
        }

        @Test
        void logSqlExceptionWithNullMessage() {
            TestSqlExecutor executor = new TestSqlExecutor(null);
            Exception ex = new RuntimeException((String) null);
            executor.logSqlException(ex, "SELECT 1");
        }

        @Test
        void logSqlExceptionWithBlankMessage() {
            TestSqlExecutor executor = new TestSqlExecutor(null);
            Exception ex = new RuntimeException("");
            executor.logSqlException(ex, "query");
        }

        @Test
        void logSqlExceptionWithPositionInfo() {
            TestSqlExecutor executor = new TestSqlExecutor(null);
            Exception ex = new RuntimeException("Error near column. Position: 25");
            executor.logSqlException(ex, "SELECT * FROM very_long_table_name WHERE col = 'value' AND other_col = 123");
        }

        @Test
        void logSqlExceptionWithoutPositionInfo() {
            TestSqlExecutor executor = new TestSqlExecutor(null);
            Exception ex = new RuntimeException("Generic SQL error without position");
            executor.logSqlException(ex, "SELECT 1");
        }

        @Test
        void databaseMessageIsTakenFromRootCause() {
            TestSqlExecutor executor = new TestSqlExecutor(null);
            SQLException cause = new SQLException("ERROR: relation \"t\" does not exist");
            Exception ex = new BadSqlGrammarException("StatementCallback", "SELECT * FROM t", cause);

            assertEquals("ERROR: relation \"t\" does not exist", executor.getDatabaseMessage(ex));
        }

        @Test
        void databaseMessageFallsBackToOwnMessageWhenNoCause() {
            TestSqlExecutor executor = new TestSqlExecutor(null);

            assertEquals("plain error", executor.getDatabaseMessage(new RuntimeException("plain error")));
        }

        @Test
        void reasonHasNoErrorPrefixAndNoPosition() {
            TestSqlExecutor executor = new TestSqlExecutor(null);
            SQLException cause = new SQLException("ERROR: relation \"t\" does not exist\n  Position: 13");
            Exception ex = new BadSqlGrammarException("StatementCallback", "SELECT * FROM t", cause);

            assertEquals("relation \"t\" does not exist", executor.getReason(ex));
        }

        @Test
        void reasonKeepsDetailLine() {
            TestSqlExecutor executor = new TestSqlExecutor(null);
            SQLException cause = new SQLException("ERROR: duplicate key value violates unique constraint \"pk\"\n"
                    + "  Detail: Key (id)=(1) already exists.");
            Exception ex = new DuplicateKeyException("PreparedStatementCallback", cause);

            assertEquals("duplicate key value violates unique constraint \"pk\"\n"
                    + "Detail: Key (id)=(1) already exists.", executor.getReason(ex));
        }

        @Test
        void pointerIsUnderBrokenPlace() {
            TestSqlExecutor executor = new TestSqlExecutor(null);
            List<String> lines = executor.describeQuery("INSERT INTO qa_3732_users (name) VALUES ('Kate')", 13);

            assertEquals(" ".repeat(19) + "^ position 13", lines.get(1));
            assertEquals('q', lines.get(0).charAt(19));
        }

        @Test
        void longQueryHasNoPointer() {
            TestSqlExecutor executor = new TestSqlExecutor(null);
            String query = "SELECT " + "a, ".repeat(60) + "b FROM t";

            assertEquals("Position: 120", executor.describeQuery(query, 120).get(1));
        }

        @Test
        void longReasonIsWrapped() {
            TestSqlExecutor executor = new TestSqlExecutor(null);
            List<String> lines = executor.wrap("word ".repeat(50).trim());

            assertTrue(lines.size() > 1);
            assertTrue(lines.stream().allMatch(line -> line.length() <= 100));
        }
    }

    @Nested
    class DefaultTruncateTest {
        @Test
        void truncatesAllTablesWithGivenQueries() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            HikariDataSource hikariDs = mock(HikariDataSource.class);
            when(hikariDs.getSchema()).thenReturn("public");
            when(mockTemplate.getDataSource()).thenReturn(hikariDs);
            String publicQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
            when(mockTemplate.queryForList(publicQuery, String.class))
                    .thenReturn(List.of("users", "orders"));
            doNothing().when(mockTemplate).execute(anyString());

            TruncatingExecutor executor = new TruncatingExecutor(null);
            try {
                Field templateField = AbstractSqlExecutor.class.getDeclaredField("template");
                templateField.setAccessible(true);
                templateField.set(executor, mockTemplate);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            executor.truncate();

            verify(mockTemplate, times(2)).execute(anyString());
        }

        @Test
        void truncatesWithMultipleQueries() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            HikariDataSource hikariDs = mock(HikariDataSource.class);
            when(hikariDs.getSchema()).thenReturn("test_schema");
            when(mockTemplate.getDataSource()).thenReturn(hikariDs);
            String testSchemaQuery = "SELECT table_name FROM information_schema.tables"
                    + " WHERE table_schema = 'test_schema'";
            when(mockTemplate.queryForList(testSchemaQuery, String.class))
                    .thenReturn(List.of("t1"));
            doNothing().when(mockTemplate).execute(anyString());

            MultiTruncatingExecutor executor = new MultiTruncatingExecutor(null);
            try {
                Field templateField = AbstractSqlExecutor.class.getDeclaredField("template");
                templateField.setAccessible(true);
                templateField.set(executor, mockTemplate);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            executor.truncate();

            verify(mockTemplate, times(2)).execute(anyString());
        }

        @Test
        void truncatesNoTablesWhenQueryReturnsEmpty() {
            JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
            HikariDataSource hikariDs = mock(HikariDataSource.class);
            when(hikariDs.getSchema()).thenReturn("public");
            when(mockTemplate.getDataSource()).thenReturn(hikariDs);
            String emptyQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
            when(mockTemplate.queryForList(emptyQuery, String.class))
                    .thenReturn(Collections.emptyList());

            TruncatingExecutor executor = new TruncatingExecutor(null);
            try {
                Field templateField = AbstractSqlExecutor.class.getDeclaredField("template");
                templateField.setAccessible(true);
                templateField.set(executor, mockTemplate);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            executor.truncate();

            verify(mockTemplate, times(0)).execute(anyString());
        }
    }

    static class TestSqlExecutor extends AbstractSqlExecutor {

        TestSqlExecutor(final DataSource dataSource) {
            super(dataSource);
        }

        @Override
        public void truncate() {
            // no-op for testing
        }
    }

    static class TruncatingExecutor extends AbstractSqlExecutor {

        TruncatingExecutor(final DataSource dataSource) {
            super(dataSource);
        }

        @Override
        public void truncate() {
            defaultTruncate(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = '%s'",
                    List.of("TRUNCATE TABLE %s CASCADE"));
        }
    }

    static class MultiTruncatingExecutor extends AbstractSqlExecutor {

        MultiTruncatingExecutor(final DataSource dataSource) {
            super(dataSource);
        }

        @Override
        public void truncate() {
            defaultTruncate(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = '%s'",
                    List.of("ALTER TABLE %s DISABLE TRIGGER ALL", "TRUNCATE TABLE %s CASCADE"));
        }
    }
}
