package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Postgres;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(Postgres.class)
public class PostgresInterpreter extends AbstractInterpreter<Postgres> {


    @Autowired(required = false)
    private PostgresSqlOperation postgresSqlOperation;

    public PostgresInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Postgres postgres, final CommandResult result) {
        String actualPostgres = getActual(postgres, result);
        CompareBuilder compare = newCompare()
                .withActual(actualPostgres)
                .withExpectedFile(postgres.getFile());

        result.setExpected(StringPrettifier.asJsonResult(compare.getExpected()));
        result.setActual(StringPrettifier.asJsonResult(actualPostgres));
        compare.exec();
        setContextBody(actualPostgres);
    }

    protected String getActual(final Postgres postgres, final CommandResult result) {
        String alias = postgres.getAlias();
        List<String> queries = getSqlList(postgres);
        LogUtil.logAllQueries(queries, alias);
        ResultUtil.addDatabaseMetaData(alias, queries, result);
        StorageOperation.StorageOperationResult applyPostgres =
                postgresSqlOperation.apply(new ListSource(queries), inject(alias));
        return toString(applyPostgres.getRaw());
    }

    private List<String> getSqlList(final Postgres postgres) {
        return postgres.getQuery().stream()
                .map(this::inject)
                .collect(Collectors.toList());
    }
}
