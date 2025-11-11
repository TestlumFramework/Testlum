package com.knubisoft.testlum.testing.framework.db.sql.executor;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.QueryResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.SUCCESS_QUERY;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.SPACE;

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

    protected final JdbcTemplate template;

    private static final Pattern STMT_SPLIT = Pattern.compile(
            ";\\s*(?:(?:/\\*.*?\\*/)|(?:--.*?$))*\\s*(?=(?i)(?:insert\\s+into|update|delete|create|alter|drop|with|do)\\b)",
            Pattern.DOTALL | Pattern.MULTILINE
    );

    public AbstractSqlExecutor(final DataSource dataSource) {
        this.template = Objects.isNull(dataSource) ? null : new JdbcTemplate(dataSource);
    }

    public abstract void truncate();

    protected void defaultTruncate(final String selectTableNamesQuery, final List<String> truncateQueries) {
        HikariDataSource dataSource = (HikariDataSource) requireNonNull(template.getDataSource());
        final String schemaName = dataSource.getSchema();
        List<String> tables = template.queryForList(format(selectTableNamesQuery, schemaName), String.class);
        for (String table : tables) {
            for (String query : truncateQueries) {
                requireNonNull(template).execute(format(query, table));
            }
        }
    }

    public List<QueryResult<Object>> executeQueries(final List<String> queries) {
        return queries.stream().map(this::executeQuery).collect(Collectors.toList());
    }

    private QueryResult<Object> executeQuery(final String query) {
        QueryResult<Object> queryResult =
                new QueryResult<>(query.replaceAll(LF, EMPTY)
                        .replaceAll(DelimiterConstant.SPACE_WITH_PLUS, SPACE)
                        .trim());

        List<String> statements = splitSqlStatements(query);

        if (statements.size() <= 1) {
            try {
                Object result = executeAppropriateQuery(queryResult.getQuery());
                queryResult.setContent(result);
            } catch (InvalidDataAccessResourceUsageException e) {
                LogUtil.logSqlException(e, query);
                throw e;
            }
            return queryResult;
        }
        Object lastResult = null;
        for (String statement : statements) {
            try {
                lastResult = executeAppropriateQuery(statement);
            } catch (InvalidDataAccessResourceUsageException e) {
                LogUtil.logSqlException(e, statement);
                throw e;
            }
        }
        queryResult.setContent(lastResult);
        return queryResult;
    }

    protected List<String> splitSqlStatements(final String script) {
        if (script == null || script.isBlank()){
            return List.of();
        }
        final String source = script.replace("\uFEFF", "");
        return STMT_SPLIT.splitAsStream(source)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
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
        int affected = requireNonNull(template).update(connection -> connection.prepareStatement(query));
        return Collections.singletonMap(COUNT, affected);
    }

    private String executeDDLQuery(final String query) {
        requireNonNull(template).execute(query);
        return SUCCESS_QUERY;
    }

    private List<Map<String, Object>> executeDQLQuery(final String query) {
        return requireNonNull(template).queryForList(query);
    }

    private Boolean checkMatchQuery(final String query,
                                    final String... operations) {
        Predicate<String> validatedQuery = s ->
                query.toUpperCase().startsWith(s)
                        || query.toUpperCase().startsWith(SPACE + s);
        return Stream.of(operations).anyMatch(validatedQuery);
    }
}
