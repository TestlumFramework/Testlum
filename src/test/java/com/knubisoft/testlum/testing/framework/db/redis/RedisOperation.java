package com.knubisoft.testlum.testing.framework.db.redis;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnRedisEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.model.global_config.Redis;
import com.knubisoft.testlum.testing.model.scenario.RedisQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.REDIS_COMMAND_NOT_FOUND;
import static java.util.Objects.isNull;

@Conditional({OnRedisEnabledCondition.class})
@Component("redisOperation")
public class RedisOperation implements StorageOperation {

    private static final String CLEAR_DATABASE = "FLUSHALL";

    private final Map<AliasEnv, StringRedisConnection> stringRedisConnection;
    @Autowired
    private GlobalTestConfigurationProvider configurationProvider;
    @Autowired
    private EnvManager envManager;

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
            if (isTruncate(Redis.class, aliasEnv, configurationProvider)
                    && Objects.equals(aliasEnv.getEnvironment(), envManager.currentEnv())) {
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
        RedisQuery redisQuery = JacksonMapperUtil.readValue(query, RedisQuery.class);
        String command = Optional.of(redisQuery.getCommand())
                .orElseThrow(() -> new DefaultFrameworkException(REDIS_COMMAND_NOT_FOUND));
        String[] args = redisQuery.getArg().toArray(new String[0]);

        Object response =
                stringRedisConnection.get(new AliasEnv(databaseAlias, envManager.currentEnv())).execute(command, args);
        return convertResult(response);
    }

    @SuppressWarnings("unchecked")
    private String convertResult(final Object result) {
        if (result instanceof byte[]) {
            return new String((byte[]) result, StandardCharsets.UTF_8);
        } else if (result instanceof List<?>) {
            return ((List<byte[]>) result).stream()
                    .map(bytes -> isNull(bytes) ? DelimiterConstant.EMPTY : new String(bytes, StandardCharsets.UTF_8))
                    .collect(Collectors.toList())
                    .toString();
        }
        return String.valueOf(result);
    }
}
