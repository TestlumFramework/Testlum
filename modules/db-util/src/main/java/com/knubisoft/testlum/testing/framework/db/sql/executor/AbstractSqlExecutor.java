package com.knubisoft.testlum.testing.framework.db.sql.executor;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.QueryResult;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.SPACE;

@Slf4j
public abstract class AbstractSqlExecutor {

    private static final String INSERT = "INSERT";
    private static final String ALTER = "ALTER";
    private static final String UPDATE = "UPDATE";
    private static final String CREATE = "CREATE";
    private static final String DELETE = "DELETE";
    private static final String TRUNCATE = "TRUNCATE";
    private static final String DROP = "DROP";
    private static final String SET = "SET";
    private static final String DO = "DO";
    private static final String CALL = "CALL";
    private static final String COUNT = "count";

    private static final Pattern STATEMENT = Pattern.compile(
            ";\\s*(?:(?:/\\*.*?\\*/)|(?:--.*?$))*\\s*(?=(?i)"
                    + "(?:insert\\s+into|update|delete|create|alter|drop|with|do)\\b)",
            Pattern.DOTALL | Pattern.MULTILINE
    );

    protected final JdbcTemplate template;

    public AbstractSqlExecutor(final DataSource dataSource) {
        this.template = Objects.isNull(dataSource) ? null : new JdbcTemplate(dataSource);
    }

    public abstract void truncate();

    protected void defaultTruncate(final String selectTableNamesQuery, final List<String> truncateQueries) {
        HikariDataSource dataSource = (HikariDataSource) Objects.requireNonNull(template.getDataSource());
        final String schemaName = dataSource.getSchema();
        List<String> tables = template.queryForList(String.format(selectTableNamesQuery, schemaName), String.class);
        for (String table : tables) {
            for (String query : truncateQueries) {
                Objects.requireNonNull(template).execute(String.format(query, table));
            }
        }
    }

    public List<QueryResult<Object>> executeQueries(final List<String> queries) {
        return queries.stream().map(this::executeQuery).toList();
    }

    private QueryResult<Object> executeQuery(final String query) {
        QueryResult<Object> queryResult = createCleanedQueryResult(query);

        List<String> statements = splitSqlStatements(query);

        Object result;
        if (shouldExecuteAsSingleQuery(statements)) {
            result = executeSingleStatement(queryResult.getQuery());
        } else {
            result = executeStatementBatch(statements);
        }

        queryResult.setContent(result);
        return queryResult;
    }

    private QueryResult<Object> createCleanedQueryResult(final String query) {
        String cleanedQuery = query.replaceAll(LF, EMPTY)
                .replaceAll(DelimiterConstant.SPACE_WITH_PLUS, SPACE)
                .trim();
        return new QueryResult<>(cleanedQuery);
    }

    private boolean shouldExecuteAsSingleQuery(final List<String> statements) {
        return statements.size() <= 1;
    }

    private Object executeStatementBatch(final List<String> statements) {
        Object lastResult = null;
        for (String statement : statements) {
            lastResult = executeSingleStatement(statement);
        }
        return lastResult;
    }

    private Object executeSingleStatement(final String statement) {
        try {
            return executeAppropriateQuery(statement);
        } catch (InvalidDataAccessResourceUsageException e) {
            logSqlException(e, statement);
            throw e;
        }
    }

    protected List<String> splitSqlStatements(final String script) {
        if (script == null || script.isBlank()) {
            return List.of();
        }
        final String source = script.replace("\uFEFF", "");
        return STATEMENT.splitAsStream(source)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public void logSqlException(final Exception ex, final String query) {
        if (StringUtils.isNotBlank(ex.getMessage())) {
            String sql = new SQLUtil().getBrokenQuery(ex, query);
            log.error(LogMessage.ERROR_SQL_QUERY,
                    ex.getMessage().replaceAll(LogFormat.newLine(), LogFormat.newLogLine()),
                    sql.replaceAll(LogFormat.newLine(), LogFormat.newLogLine()));
        } else {
            log.error(LogMessage.ERROR_SQL_QUERY,
                    ex.toString().replaceAll(LogFormat.newLine(), LogFormat.newLogLine()));
        }
    }

    private Object executeAppropriateQuery(final String query) {
        if (checkMatchQuery(query, INSERT, UPDATE, DELETE)) {
            return executeDMLQuery(query);
        } else if (checkMatchQuery(query, ALTER, CREATE, TRUNCATE, DROP, SET, DO, CALL)) {
            return executeDDLQuery(query);
        }
        return executeDQLQuery(query);
    }

    private Map<String, Integer> executeDMLQuery(final String query) {
        int affected = Objects.requireNonNull(template).update(connection -> connection.prepareStatement(query));
        return Collections.singletonMap(COUNT, affected);
    }

    private String executeDDLQuery(final String query) {
        Objects.requireNonNull(template).execute(query);
        return LogMessage.SUCCESS_QUERY;
    }

    private List<Map<String, Object>> executeDQLQuery(final String query) {
        return Objects.requireNonNull(template).queryForList(query);
    }

    private Boolean checkMatchQuery(final String query,
                                    final String... operations) {
        Predicate<String> validatedQuery = s ->
                query.toUpperCase().startsWith(s)
                        || query.toUpperCase().startsWith(StringUtils.SPACE + s);
        return Stream.of(operations).anyMatch(validatedQuery);
    }

    private static class SQLUtil {
        private static final int CUT_LIMIT = 100;
        private static final int OFFSET_LIMIT = 2;
        private static final Pattern BAD_SQL_POSITION_PATTERN = Pattern.compile("Position: ([0-9]+)");

        public String getBrokenQuery(final Exception ex, final String query) {
            final int position = getSqlPositionFromException(ex) - OFFSET_LIMIT;
            return StringUtils.abbreviate(query, position, CUT_LIMIT);
        }

        private int getSqlPositionFromException(final Exception ex) {
            Matcher m = BAD_SQL_POSITION_PATTERN.matcher(ex.getMessage());
            try {
                if (m.find()) {
                    return Integer.parseInt(m.group(1));
                }
            } catch (Exception e) {
                log.debug("Failed to extract SQL error position: {}", e.getMessage());
            }
            return 0;
        }
    }
}
