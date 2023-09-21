package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.db.util.LogUtil;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.db.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Postgres;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;

@Slf4j
@InterpreterForClass(Postgres.class)
public class PostgresInterpreter extends AbstractInterpreter<Postgres> {

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
        setContextBody(actualPostgres);
    }

    protected String getActual(final Postgres postgres, final CommandResult result) {
        String alias = postgres.getAlias();
        List<String> queries = postgres.getQuery();
        LogUtil.logAllQueries(queries, alias);
        ResultUtil.addDatabaseMetaData(alias, queries, result);
        StorageOperationResult applyPostgres = postgresSqlOperation.apply(new ListSource(queries), alias);
        return toString(applyPostgres.getRaw());
    }
}
