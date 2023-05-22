package com.knubisoft.testlum.testing.framework.db.redis;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnRedisEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.model.scenario.QueryParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Conditional({OnRedisEnabledCondition.class})
@Component
public class RedisOperation implements StorageOperation {

    private static final String CLEAR_DATABASE = "FLUSHALL";

    private final Map<AliasEnv, StringRedisConnection> stringRedisConnection;

    public RedisOperation(@Autowired(required = false)
                          final Map<AliasEnv, StringRedisConnection> stringRedisConnection) {
        this.stringRedisConnection = stringRedisConnection;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseAlias) {
        return new StorageOperationResult(applyQueries(source.getQueries(), databaseAlias));
    }

    @Override
    public void clearSystem() {
        stringRedisConnection.forEach((aliasEnv, redisConnection) -> {
            if (Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                redisConnection.execute(CLEAR_DATABASE);
            }
        });
    }

    private List<QueryResult<String>> applyQueries(final List<String> queries, final String databaseAlias) {
        return queries.stream()
                .map(e -> new QueryResult<>(e, executeQuery(e, databaseAlias)))
                .collect(Collectors.toList());
    }

    private String executeQuery(final String query, final String databaseAlias) {
//        LinkedList<String> instructions = Arrays.stream(query.split(DelimiterConstant.HASH))
//                .map(String::trim)
//                .filter(StringUtils::isNotBlank)
//                .collect(Collectors.toCollection(LinkedList::new));
        QueryParameters redisQuery = JacksonMapperUtil.readValue(query, QueryParameters.class);

        String command = Objects.requireNonNull(redisQuery.getCommand());
        String[] args = redisQuery.getArg().toArray(new String[0]);

        Object response = stringRedisConnection.get(AliasEnv.build(databaseAlias)).execute(command, args);
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
