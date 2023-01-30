package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.redis.RedisOperation;
import com.knubisoft.cott.testing.framework.db.source.ListSource;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

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
        String actual = getActual(redis, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpectedFile(redis.getFile());

        result.setActual(PrettifyStringJson.getJSONResult(actual));
        result.setExpected(PrettifyStringJson.getJSONResult(comparator.getExpected()));

        comparator.exec();
        setContextBody(actual);
    }

    protected String getActual(final Redis redis, final CommandResult result) {
        String alias = redis.getAlias();
        final List<String> queries = getRedisQueryList(redis);
        LogUtil.logAllQueries(queries, alias);
        ResultUtil.addDatabaseMetaData(alias, queries, result);
        final StorageOperation.StorageOperationResult apply = redisOperation.apply(new ListSource(queries),
                dependencies.getEnvironment() + UNDERSCORE + alias);
        return toString(apply.getRaw());
    }

    private List<String> getRedisQueryList(final Redis query) {
        return query.getQuery().stream()
                .map(this::inject)
                .collect(Collectors.toList());
    }
}
