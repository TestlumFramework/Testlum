package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractDatabaseInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.model.scenario.Redis;
import com.knubisoft.testlum.testing.model.scenario.RedisQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Slf4j
@InterpreterForClass(Redis.class)
public class RedisInterpreter extends AbstractDatabaseInterpreter<Redis> {

    private static final String REDIS_QUERY = LogFormat.table("Query", "{} {}");

    @Autowired(required = false)
    @Qualifier("redisOperation")
    private AbstractStorageOperation redisOperation;

    public RedisInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected AbstractStorageOperation getOperation() {
        return redisOperation;
    }

    @Override
    protected String getAlias(final Redis command) {
        return command.getAlias();
    }

    @Override
    protected void setAlias(final Redis command, final String alias) {
        command.setAlias(alias);
    }

    @Override
    protected List<String> getQueries(final Redis command) {
        return command.getQuery().stream()
                .map(this::toString)
                .toList();
    }

    @Override
    protected String getFile(final Redis command) {
        return command.getFile();
    }

    @Override
    protected void logAllQueries(final List<String> queries, final String alias) {
        log.info(ALIAS_LOG, alias);
        queries.stream()
                .map(q -> jacksonService.readValue(q, RedisQuery.class))
                .forEach(query -> log.info(REDIS_QUERY, query.getCommand(),
                        String.join(DelimiterConstant.SPACE, query.getArg())));
    }
}
