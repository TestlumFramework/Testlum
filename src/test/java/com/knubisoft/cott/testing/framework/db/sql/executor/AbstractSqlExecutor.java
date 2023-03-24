package com.knubisoft.cott.testing.framework.db.sql.executor;

import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.SUCCESS_QUERY;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.SPACE;

public abstract class AbstractSqlExecutor {

    private static final String SPACE_PLUS = " +";
    private static final String INSERT = "INSERT";
    private static final String ALTER = "ALTER";
    private static final String UPDATE = "UPDATE";
    private static final String CREATE = "CREATE";
    private static final String DELETE = "DELETE";
    private static final String TRUNCATE = "TRUNCATE";
    private static final String DROP = "DROP";
    private static final String SET = "SET";
    private static final String COUNT = "count";

    protected final JdbcTemplate template;

    public AbstractSqlExecutor(final DataSource dataSource) {
        this.template = dataSource == null ? null : new JdbcTemplate(dataSource);
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

    public List<StorageOperation.QueryResult<Object>> executeQueries(final List<String> queries) {
        return queries.stream().map(this::executeQuery).collect(Collectors.toList());
    }

    private StorageOperation.QueryResult<Object> executeQuery(final String query) {
        StorageOperation.QueryResult<Object> queryResult =
                new StorageOperation.QueryResult<>(query.replaceAll(LF, EMPTY)
                        .replaceAll(SPACE_PLUS, SPACE)
                        .trim());

        try {
            Object result = executeAppropriateQuery(queryResult.getQuery());
            queryResult.setContent(result);
        } catch (DataAccessException e) {
            LogUtil.logSqlException(e, queryResult.getQuery());
            throw e;
        }
        return queryResult;
    }

    private Object executeAppropriateQuery(final String query) {
        if (checkMatchQuery(query, INSERT, UPDATE, DELETE)) {
            return executeDMLQuery(query);
        } else if (checkMatchQuery(query, ALTER, CREATE, TRUNCATE, DROP, SET)) {
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
