package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Postgres;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.REGEX_MANY_SPACES;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;
import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Postgres.class)
public class PostgresInterpreter extends AbstractInterpreter<Postgres> {

    //LOGS
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String QUERY = format(TABLE_FORMAT, "Query", "{}");
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");

    //RESULT
    private static final String QUERIES = "Queries";
    private static final String DATABASE_ALIAS = "Database alias";

    @Autowired(required = false)
    @Qualifier("postgresOperation")
    private AbstractStorageOperation postgresSqlOperation;

    public PostgresInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Postgres o, final CommandResult result) {
        Postgres postgres = injectCommand(o);
        String actualPostgres = getActual(postgres, result);
        CompareBuilder compare = newCompare()
                .withActual(actualPostgres)
                .withExpected(getContentIfFile(postgres.getFile()));

        result.setExpected(StringPrettifier.asJsonResult(compare.getExpected()));
        result.setActual(StringPrettifier.asJsonResult(actualPostgres));
        compare.exec();
        setContextBody(getContextBodyKey(postgres.getFile()), actualPostgres);
    }

    protected String getActual(final Postgres postgres, final CommandResult result) {
        String alias = postgres.getAlias();
        List<String> queries = postgres.getQuery();
        logAllQueries(queries, alias);
        addDatabaseMetaData(alias, queries, result);
        StorageOperationResult applyPostgres = postgresSqlOperation.apply(new ListSource(queries), alias);
        return toString(applyPostgres.getRaw());
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
