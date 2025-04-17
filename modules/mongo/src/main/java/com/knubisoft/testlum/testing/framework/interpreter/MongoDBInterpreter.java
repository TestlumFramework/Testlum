package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Mongo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.REGEX_MANY_SPACES;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Mongo.class)
public class MongoDBInterpreter extends AbstractInterpreter<Mongo> {

    //LOGS
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String QUERY = format(TABLE_FORMAT, "Query", "{}");
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");

    //RESULT
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
        String actual = getActual(mongo, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpected(getContentIfFile(mongo.getFile()));

        result.setActual(StringPrettifier.asJsonResult(actual));
        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));

        comparator.exec();
        setContextBody(getContextBodyKey(mongo.getFile()), actual);
    }

    private String getActual(final Mongo mongo, final CommandResult result) {
        String alias = mongo.getAlias();
        List<String> queries = mongo.getQuery();
        logAllQueries(queries, mongo.getAlias());
        addDatabaseMetaData(alias, queries, result);
        ListSource listSource = new ListSource(queries);
        StorageOperationResult apply = mongoOperation.apply(listSource, alias);
        return toString(apply.getRaw());
    }

    private void logAllQueries(final List<String> queries, final String alias) {
        log.info(ALIAS_LOG, alias);
        queries.forEach(query -> log.info(QUERY, query.replaceAll(REGEX_MANY_SPACES, SPACE)));
    }

    private void addDatabaseMetaData(final String databaseAlias,
                                     final List<String> queries,
                                     final CommandResult result) {
        result.put(DATABASE_ALIAS, databaseAlias);
        result.put(QUERIES, queries);
    }
}
