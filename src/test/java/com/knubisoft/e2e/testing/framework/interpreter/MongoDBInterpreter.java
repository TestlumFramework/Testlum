package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.mongodb.MongoOperation;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.LogUtil;
import com.knubisoft.e2e.testing.framework.util.PrettifyStringJson;
import com.knubisoft.e2e.testing.framework.util.ResultUtil;
import com.knubisoft.e2e.testing.model.scenario.Mongo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(Mongo.class)
public class MongoDBInterpreter extends AbstractInterpreter<Mongo> {

    @Autowired(required = false)
    private MongoOperation mongoOperation;

    public MongoDBInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Mongo mongo, final CommandResult result) {
        String actual = getActual(mongo, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpectedFile(mongo.getFile());

        result.setActual(PrettifyStringJson.getJSONResult(actual));
        result.setExpected(PrettifyStringJson.getJSONResult(comparator.getExpected()));

        comparator.exec();
        setContextBody(actual);
    }

    private String getActual(final Mongo mongo, final CommandResult result) {
        String alias = mongo.getAlias();
        List<String> queries = getMongoQueryList(mongo);
        LogUtil.logAllQueries(queries, mongo.getAlias());
        ResultUtil.addDatabaseMetaData(alias, queries, result);
        ListSource listSource = new ListSource(queries);
        StorageOperation.StorageOperationResult apply = mongoOperation.apply(listSource, alias);
        return toString(apply.getRaw());
    }

    private List<String> getMongoQueryList(final Mongo mongo) {
        return mongo.getQuery().stream().map(this::inject).collect(Collectors.toList());
    }
}
