package com.knubisoft.e2e.testing.framework.db.sql.executor;

import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.CollectionUtils;
import ru.yandex.clickhouse.ClickHouseDataSource;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.SPACE;

public abstract class AbstractSqlExecutor {

    private static final String PK_NAME = "id";
    private static final String SPACE_PLUS = " +";
    private static final String INSERT = "INSERT";
    private static final String ALTER = "ALTER";
    private static final String UPDATE = "UPDATE";
    private static final String CREATE = "CREATE";
    private static final String DELETE = "DELETE";
    private static final String COUNT = "count";

    protected final JdbcTemplate template;

    public AbstractSqlExecutor(final DataSource dataSource) {
        this.template = dataSource == null ? null : new JdbcTemplate(dataSource);
    }

    public abstract void truncate();

    protected abstract List<Number> getAffectedKeys(List<Map<String, Object>> keyList);

    public List<StorageOperation.QueryResult<Object>> executeQueries(final List<String> queries) {
        try {
            return queries.stream().map(this::executeQuery).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private StorageOperation.QueryResult<Object> executeQuery(final String query) {
        StorageOperation.QueryResult<Object> queryResult =
                new StorageOperation.QueryResult<>(query.replaceAll(LF, EMPTY)
                        .replaceAll(SPACE_PLUS, SPACE)
                        .trim());

        Object result = executeAppropriateQuery(queryResult.getQuery());
        queryResult.setContent(result);
        return queryResult;
    }

    private Object executeAppropriateQuery(final String query) {
        if (checkMatchQuery(query, INSERT, ALTER, CREATE)) {
            return executeInsertQuery(query);
        } else if (checkMatchQuery(query, UPDATE,  DELETE)) {
            return executeDmlQuery(query);
        }
        return executeDqlQuery(query);
    }

    private List<Number> executeInsertQuery(final String query) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        executeDdlQuery(query, keyHolder);
        List<Map<String, Object>> keyList = keyHolder.getKeyList();
        return CollectionUtils.isEmpty(keyList) ? Collections.emptyList() : getAffectedKeys(keyList);
    }

    private void executeDdlQuery(final String query, final KeyHolder keyHolder) {
        if (this.template.getDataSource() instanceof ClickHouseDataSource) {
            requireNonNull(template).update(connection ->
                    connection.prepareStatement(query), keyHolder);
        } else {
            requireNonNull(template).update(connection ->
                    connection.prepareStatement(query, new String[]{PK_NAME}), keyHolder);
        }
    }

    private Map<String, Integer> executeDmlQuery(final String query) {
        int affected = requireNonNull(template).update(connection -> connection.prepareStatement(query));
        return Collections.singletonMap(COUNT, affected);
    }

    private List<Map<String, Object>> executeDqlQuery(final String query) {
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
