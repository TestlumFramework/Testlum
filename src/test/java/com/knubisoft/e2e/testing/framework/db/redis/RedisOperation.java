package com.knubisoft.e2e.testing.framework.db.redis;

import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.db.source.Source;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class RedisOperation implements StorageOperation {

    private static final String CLEAR_DATABASE = "FLUSHALL";

    private final Map<String, StringRedisConnection> stringRedisConnection;

    public RedisOperation(@Autowired(required = false) final Map<String, StringRedisConnection> stringRedisConnection) {
        this.stringRedisConnection = stringRedisConnection;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseName) {
        return new StorageOperationResult(applyQueries(source.getQueries(), databaseName));
    }

    @Override
    public void clearSystem() {
        for (Map.Entry<String, StringRedisConnection> entry : stringRedisConnection.entrySet()) {
            apply(new ListSource(Collections.singletonList(CLEAR_DATABASE)), entry.getKey());
        }
    }

    private List<QueryResult<String>> applyQueries(final List<String> queries, final String databaseName) {
        return queries.stream()
                .map(e -> new QueryResult<>(e, executeQuery(e, databaseName)))
                .collect(Collectors.toList());
    }

    private String executeQuery(final String query, final String databaseName) {
        LinkedList<String> instructions = Arrays.stream(query.split(DelimiterConstant.HASH))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedList::new));

        String command = Objects.requireNonNull(instructions.poll());
        String[] args = instructions.toArray(new String[0]);

        Object response = stringRedisConnection.get(databaseName).execute(command, args);
        return convertResult(response);
    }

    private String convertResult(final Object result) {
        if (result instanceof byte[]) {
            return new String((byte[]) result, StandardCharsets.UTF_8);
        } else {
            return String.valueOf(result);
        }
    }
}
