package com.knubisoft.testlum.testing.framework.interpreter;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.REGEX_MANY_SPACES;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(SqlDatabase.class)
public class SqlDatabaseInterpreter extends AbstractInterpreter<SqlDatabase> {

    //LOGS
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String QUERY = format(TABLE_FORMAT, "Query", "{}");
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");

    //RESULT
    private static final String QUERIES = "Queries";
    private static final String DATABASE_ALIAS = "Database alias";

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

    protected String getActual(final SqlDatabase sqlDatabase, final CommandResult result) {
        String alias = sqlDatabase.getAlias();
        List<String> queries = sqlDatabase.getQuery();
        logAllQueries(queries, alias);
        addDatabaseMetaData(alias, queries, result);
        StorageOperationResult applySqlDatabase = sqlDatabaseOperation.apply(new ListSource(queries), alias);
        return toString(applySqlDatabase.getRaw());
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