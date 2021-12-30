package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.PrettifyStringJson;
import com.knubisoft.e2e.testing.model.scenario.Postgres;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
@InterpreterForClass(Postgres.class)
public class PostgresInterpreter extends AbstractInterpreter<Postgres> {

    private static final String SQL_LOG_TEMPLATE = "%10s %-100s";

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

        result.setExpected(PrettifyStringJson.getJSONResult(compare.getExpected()));
        result.setActual(PrettifyStringJson.getJSONResult(actualPostgres));

        compare.exec();
        setContextBody(actualPostgres);
    }

    protected String getActual(final Postgres postgres, final CommandResult result) {
        List<String> queries = getSqlList(postgres);
        result.put("queries", new ArrayList<>(queries));
        StorageOperation.StorageOperationResult applyPostgres =
                postgresSqlOperation.apply(new ListSource(queries), inject(postgres.getAlias()));
        return toString(applyPostgres.getRaw());
    }

    private List<String> getSqlList(final Postgres postgres) {
        List<String> queriesPostgres = postgres.getQuery().stream()
                .map(this::inject)
                .collect(Collectors.toList());
        queriesPostgres.forEach(it -> log.info(format(SQL_LOG_TEMPLATE, EMPTY, it)));
        return queriesPostgres;
    }
}
