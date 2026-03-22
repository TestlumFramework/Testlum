package com.knubisoft.testlum.testing.framework.db.sql.executor;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.QueryResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AbstractSqlExecutor {

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
    protected final LogUtil logUtil;

    public AbstractSqlExecutor(final DataSource dataSource, final LogUtil logUtil) {
        this.template = Objects.isNull(dataSource) ? null : new JdbcTemplate(dataSource);
        this.logUtil = logUtil;
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
        QueryResult<Object> queryResult =
                new QueryResult<>(query.replaceAll(StringUtils.LF, StringUtils.EMPTY)
                        .replaceAll(DelimiterConstant.SPACE_WITH_PLUS, StringUtils.SPACE)
                        .trim());

        try {
            Object result = executeAppropriateQuery(queryResult.getQuery());
            queryResult.setContent(result);
        } catch (InvalidDataAccessResourceUsageException e) {
            logUtil.logSqlException(e, queryResult.getQuery());
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
}
