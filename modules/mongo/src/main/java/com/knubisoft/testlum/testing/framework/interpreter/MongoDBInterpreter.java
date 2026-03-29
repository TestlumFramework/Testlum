package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Mongo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Slf4j
@InterpreterForClass(Mongo.class)
public class MongoDBInterpreter extends AbstractInterpreter<Mongo> {

    private static final String QUERY = LogFormat.table("Query");

    private static final String QUERIES = "Queries";
    private static final String DATABASE_ALIAS = "Database alias";
    @Autowired(required = false)
    @Qualifier("mongoOperation")
    private AbstractStorageOperation mongoOperation;

    public MongoDBInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Mongo o, final CommandResult result) {
        Mongo mongo = injectCommand(o);
        ensureAlias(mongo::getAlias, mongo::setAlias);
        String actual = getActual(mongo, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpected(getContentIfFile(mongo.getFile()));

        result.setActual(stringPrettifier.asJsonResult(actual));
        result.setExpected(stringPrettifier.asJsonResult(comparator.getExpected()));

        comparator.exec();
        setContextBody(getContextBodyKey(mongo.getFile()), actual);
    }

    private String getActual(final Mongo mongo, final CommandResult result) {
        String alias = mongo.getAlias();
        List<String> queries = mongo.getQuery();
        logAllQueries(queries, mongo.getAlias());
        addDatabaseMetaData(alias, queries, result);
        ListSource listSource = new ListSource(queries);
        AbstractStorageOperation.StorageOperationResult apply = mongoOperation.apply(listSource, alias);
        return toString(apply.getRaw());
    }

    private void logAllQueries(final List<String> queries, final String alias) {
        log.info(ALIAS_LOG, alias);
        queries.forEach(query -> log.info(QUERY,
                query.replaceAll(DelimiterConstant.REGEX_MANY_SPACES, DelimiterConstant.SPACE)));
    }

    private void addDatabaseMetaData(final String databaseAlias,
                                     final List<String> queries,
                                     final CommandResult result) {
        result.put(DATABASE_ALIAS, databaseAlias);
        result.put(QUERIES, queries);
    }
}
