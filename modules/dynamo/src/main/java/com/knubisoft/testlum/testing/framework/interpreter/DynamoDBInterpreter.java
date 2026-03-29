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
import com.knubisoft.testlum.testing.model.scenario.Dynamo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Slf4j
@InterpreterForClass(Dynamo.class)
public class DynamoDBInterpreter extends AbstractInterpreter<Dynamo> {

    private static final String QUERY = LogFormat.table("Query");

    private static final String QUERIES = "Queries";
    private static final String DATABASE_ALIAS = "Database alias";
    @Autowired(required = false)
    @Qualifier("dynamoOperation")
    private AbstractStorageOperation dynamoDBOperation;

    public DynamoDBInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Dynamo o, final CommandResult result) {
        Dynamo ddb = injectCommand(o);
        ensureAlias(ddb::getAlias, ddb::setAlias);
        String actual = getActual(ddb, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpected(getContentIfFile(ddb.getFile()));

        result.setActual(stringPrettifier.asJsonResult(actual));
        result.setExpected(stringPrettifier.asJsonResult(comparator.getExpected()));

        comparator.exec();
        setContextBody(getContextBodyKey(ddb.getFile()), actual);
    }

    protected String getActual(final Dynamo ddb, final CommandResult result) {
        String alias = ddb.getAlias();
        List<String> queries = ddb.getQuery();
        logAllQueries(queries, alias);
        addDatabaseMetaData(alias, queries, result);
        AbstractStorageOperation.StorageOperationResult apply = dynamoDBOperation.apply(new ListSource(queries), alias);
        return jacksonService.writeAsStringFieldVisibility(apply.getRaw());
    }

    private void logAllQueries(final List<String> queries, final String alias) {
        log.info(ALIAS_LOG, alias);
        queries.forEach(query -> log.info(QUERY, query.replaceAll(
                DelimiterConstant.REGEX_MANY_SPACES, DelimiterConstant.SPACE)));
    }

    private void addDatabaseMetaData(final String databaseAlias,
                                     final List<String> queries,
                                     final CommandResult result) {
        result.put(DATABASE_ALIAS, databaseAlias);
        result.put(QUERIES, queries);
    }
}
