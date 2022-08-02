package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.ListSource;
import com.knubisoft.cott.testing.framework.db.sql.ClickhouseOperation;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.PrettifyStringJson;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.Clickhouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(Clickhouse.class)
public class ClickhouseInterpreter extends AbstractInterpreter<Clickhouse> {

    @Autowired(required = false)
    private ClickhouseOperation clickhouseOperation;

    public ClickhouseInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Clickhouse clickhouse, final CommandResult result) {
        String actual = getActual(clickhouse, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpectedFile(clickhouse.getFile());

        result.setExpected(PrettifyStringJson.getJSONResult(comparator.getExpected()));
        result.setActual(PrettifyStringJson.getJSONResult(actual));

        comparator.exec();
        setContextBody(actual);
    }

    protected String getActual(final Clickhouse clickhouse, final CommandResult result) {
        String alias = clickhouse.getAlias();
        List<String> queries = getSqlList(clickhouse);
        LogUtil.logAllQueries(queries, alias);
        ResultUtil.addDatabaseMetaData(alias, queries, result);
        StorageOperation.StorageOperationResult applyClickhouse = clickhouseOperation.apply(new ListSource(queries),
                alias);
        return toString(applyClickhouse.getRaw());
    }

    private List<String> getSqlList(final Clickhouse clickhouse) {
        return clickhouse.getQuery().stream()
                .map(this::inject)
                .collect(Collectors.toList());
    }
}
