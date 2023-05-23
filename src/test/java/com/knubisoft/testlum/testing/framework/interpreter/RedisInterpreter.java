package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.redis.RedisOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Redis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(Redis.class)
public class RedisInterpreter extends AbstractInterpreter<Redis> {

    @Autowired(required = false)
    private RedisOperation redisOperation;

    public RedisInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Redis redis, final CommandResult result) {
        String expected = inject(getContentIfFile(redis.getFile()));
        String actual = getActual(redis, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpected(expected);

        result.setActual(StringPrettifier.asJsonResult(actual));
        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));

        comparator.exec();
        setContextBody(actual);
    }

    protected String getActual(final Redis redis, final CommandResult result) {
        String alias = redis.getAlias();
        final List<String> queries = getRedisQueryList(redis);
        LogUtil.logAllQueries(queries, redis.getAlias());
        ResultUtil.addDatabaseMetaData(alias, queries, result);
        final StorageOperation.StorageOperationResult apply = redisOperation.apply(new ListSource(queries),
                alias);
        return toString(apply.getRaw());
    }

    private List<String> getRedisQueryList(final Redis query) {
        return query.getQuery().stream()
                .map(this::inject)
                .collect(Collectors.toList());
    }
}
