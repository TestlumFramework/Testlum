package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Redis;
import com.knubisoft.testlum.testing.model.scenario.RedisQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Redis.class)
public class RedisInterpreter extends AbstractInterpreter<Redis> {

    private static final String QUERIES = "Queries";
    private static final String DATABASE_ALIAS = "Database alias";
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String REDIS_QUERY = format(TABLE_FORMAT, "Query", "{} {}");
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    @Autowired(required = false)
    @Qualifier("redisOperation")
    private AbstractStorageOperation redisOperation;

    public RedisInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Redis o, final CommandResult result) {
        Redis redis = injectCommand(o);
        checkAlias(redis);
        String actual = getActual(redis, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpected(getContentIfFile(redis.getFile()));

        result.setActual(StringPrettifier.asJsonResult(actual));
        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));

        comparator.exec();
        setContextBody(getContextBodyKey(redis.getFile()), actual);
    }

    private void checkAlias(final Redis redis) {
        if (redis.getAlias() == null) {
            redis.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    protected String getActual(final Redis redis, final CommandResult result) {
        String alias = redis.getAlias();
        List<RedisQuery> redisQueries = redis.getQuery();
        List<String> queries = convertToStringQueries(redisQueries);
        logAllRedisQueries(redisQueries, alias);
        addDatabaseMetaData(alias, queries, result);
        StorageOperationResult apply = redisOperation.apply(new ListSource(queries), alias);
        return toString(apply.getRaw());
    }

    private List<String> convertToStringQueries(final List<RedisQuery> redisQueries) {
        return redisQueries.stream()
                .map(this::toString)
                .collect(Collectors.toList());
    }

    private void logAllRedisQueries(final List<RedisQuery> redisQueries, final String alias) {
        log.info(ALIAS_LOG, alias);
        redisQueries.forEach(query ->
                log.info(REDIS_QUERY, query.getCommand(), String.join(SPACE, query.getArg())));
    }

    private void addDatabaseMetaData(final String databaseAlias,
                                    final List<String> queries,
                                    final CommandResult result) {
        result.put(DATABASE_ALIAS, databaseAlias);
        result.put(QUERIES, queries);
    }
}

