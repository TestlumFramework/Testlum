package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Oracle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.REGEX_MANY_SPACES;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Oracle.class)
public class OracleInterpreter extends AbstractInterpreter<Oracle> {

    //LOGS
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String QUERY = format(TABLE_FORMAT, "Query", "{}");
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");

    //RESULT
    private static final String QUERIES = "Queries";
    private static final String DATABASE_ALIAS = "Database alias";
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    @Autowired(required = false)
    @Qualifier("oracleOperation")
    private AbstractStorageOperation oracleOperation;

    public OracleInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Oracle o, final CommandResult result) {
        Oracle oracle = injectCommand(o);
        checkAlias(oracle);
        String actual = getActual(oracle, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpected(getContentIfFile(oracle.getFile()));

        result.setActual(StringPrettifier.asJsonResult(actual));
        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));

        comparator.exec();
        setContextBody(getContextBodyKey(oracle.getFile()), actual);
    }

    private void checkAlias(final Oracle oracle) {
        if (oracle.getAlias() == null) {
            oracle.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    protected String getActual(final Oracle oracle, final CommandResult result) {
        String alias = oracle.getAlias();
        List<String> queries = oracle.getQuery();
        logAllQueries(queries, oracle.getAlias());
        addDatabaseMetaData(alias, queries, result);
        StorageOperationResult applyOracle = oracleOperation.apply(new ListSource(queries), alias);
        return toString(applyOracle.getRaw());
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
