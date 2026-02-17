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
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.SqlDatabase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Slf4j
@InterpreterForClass(SqlDatabase.class)
public class SqlDatabaseInterpreter extends AbstractInterpreter<SqlDatabase> {

    private static final String QUERY = LogFormat.table("Query");
    private static final String ALIAS_LOG = LogFormat.table("Alias");

    private static final String QUERIES = "Queries";
    private static final String DATABASE_ALIAS = "Database alias";
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    @Autowired(required = false)
    @Qualifier("sqlDatabaseOperation")
    private AbstractStorageOperation sqlDatabaseOperation;

    public SqlDatabaseInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final SqlDatabase o, final CommandResult result) {
        SqlDatabase database = injectCommand(o);
        String actualSqlDatabase = getActual(database, result);
        CompareBuilder compare = newCompare()
                .withActual(actualSqlDatabase)
                .withExpected(getContentIfFile(database.getFile()));

        result.setExpected(StringPrettifier.asJsonResult(compare.getExpected()));
        result.setActual(StringPrettifier.asJsonResult(actualSqlDatabase));
        compare.exec();
        setContextBody(getContextBodyKey(database.getFile()), actualSqlDatabase);
    }

    private void checkAlias(final SqlDatabase sqlDatabase) {
        if (sqlDatabase.getAlias() == null) {
            sqlDatabase.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    protected String getActual(final SqlDatabase sqlDatabase, final CommandResult result) {
        String alias = sqlDatabase.getAlias();
        List<String> queries = sqlDatabase.getQuery();
        logAllQueries(queries, alias);
        addDatabaseMetaData(alias, queries, result);
        AbstractStorageOperation.StorageOperationResult applySqlDatabase =
                sqlDatabaseOperation.apply(new ListSource(queries), alias);
        return toString(applySqlDatabase.getRaw());
    }

    private void logAllQueries(final List<String> queries, final String alias) {
        log.info(ALIAS_LOG, alias);
        queries.forEach(query -> log.info(QUERY,
                query.replaceAll(DelimiterConstant.REGEX_MANY_SPACES, StringUtils.EMPTY)));
    }

    private void addDatabaseMetaData(final String databaseAlias,
                                     final List<String> queries,
                                     final CommandResult result) {
        result.put(DATABASE_ALIAS, databaseAlias);
        result.put(QUERIES, queries);
    }
}