package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class AbstractDatabaseInterpreter<T extends AbstractCommand> extends AbstractInterpreter<T> {

    private static final String QUERY = LogFormat.table("Query");
    private static final String QUERIES = "Queries";
    private static final String DATABASE_ALIAS = "Database alias";

    protected AbstractDatabaseInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    protected abstract AbstractStorageOperation getOperation();

    protected abstract String getAlias(T command);

    protected abstract void setAlias(T command, String alias);

    protected abstract List<String> getQueries(T command);

    protected abstract String getFile(T command);

    @Override
    protected void acceptImpl(final T o, final CommandResult result) {
        T command = injectCommand(o);
        ensureAlias(() -> getAlias(command), alias -> setAlias(command, alias));
        String actual = executeQuery(command, result);
        CompareBuilder compare = newCompare()
                .withActual(actual)
                .withExpected(getContentIfFile(getFile(command)));
        result.setExpected(stringPrettifier.asJsonResult(compare.getExpected()));
        result.setActual(stringPrettifier.asJsonResult(actual));
        compare.exec();
        setContextBody(getContextBodyKey(getFile(command)), actual);
    }

    private String executeQuery(final T command, final CommandResult result) {
        String alias = getAlias(command);
        List<String> queries = getQueries(command);
        logAllQueries(queries, alias);
        addDatabaseMetaData(alias, queries, result);
        AbstractStorageOperation.StorageOperationResult operationResult =
                getOperation().apply(new ListSource(queries), alias);
        return toString(operationResult.getRaw());
    }

    private void logAllQueries(final List<String> queries, final String alias) {
        log.info(ALIAS_LOG, alias);
        queries.forEach(query -> log.info(QUERY,
                query.replaceAll(DelimiterConstant.REGEX_MANY_SPACES, DelimiterConstant.SPACE)));
    }

    private void addDatabaseMetaData(final String alias, final List<String> queries, final CommandResult result) {
        result.put(DATABASE_ALIAS, alias);
        result.put(QUERIES, queries);
    }
}
