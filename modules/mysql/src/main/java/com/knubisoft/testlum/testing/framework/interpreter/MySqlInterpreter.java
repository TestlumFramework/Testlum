package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Mysql;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.REGEX_MANY_SPACES;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Mysql.class)
public class MySqlInterpreter extends AbstractInterpreter<Mysql> {

    //LOGS
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String QUERY = format(TABLE_FORMAT, "Query", "{}");
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");

    //RESULT
    private static final String QUERIES = "Queries";
    private static final String DATABASE_ALIAS = "Database alias";

    @Autowired(required = false)
    @Qualifier("mySqlOperation")
    private AbstractStorageOperation mySqlOperation;

    public MySqlInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Mysql o, final CommandResult result) {
        Mysql mysql = injectCommand(o);
        String actual = getActual(mysql, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpected(getContentIfFile(mysql.getFile()));

        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));
        result.setActual(StringPrettifier.asJsonResult(actual));

        comparator.exec();
        setContextBody(getContextBodyKey(mysql.getFile()), actual);
    }

    protected String getActual(final Mysql mysql, final CommandResult result) {
        String alias = mysql.getAlias();
        List<String> queries = mysql.getQuery();
        logAllQueries(queries, alias);
        addDatabaseMetaData(alias, queries, result);
        StorageOperationResult applyMySql = mySqlOperation.apply(new ListSource(queries), alias);
        return toString(applyMySql.getRaw());
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
