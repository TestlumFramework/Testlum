package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.db.sql.ClickhouseOperation;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.LogUtil;
import com.knubisoft.e2e.testing.model.scenario.Clickhouse;
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

        result.setExpected(comparator.getExpected());
        result.setActual(actual);

        comparator.exec();
        setContextBody(actual);
    }

    protected String getActual(final Clickhouse clickhouse, final CommandResult result) {
        List<String> sqls = getSqlList(clickhouse);
        LogUtil.logAllQueries(sqls, clickhouse.getAlias());
        result.put("sqls", sqls);
        StorageOperation.StorageOperationResult applyClickhouse = clickhouseOperation.apply(new ListSource(sqls),
                clickhouse.getAlias());
        return toString(applyClickhouse.getRaw());
    }

    private List<String> getSqlList(final Clickhouse clickhouse) {
        return clickhouse.getQuery().stream()
                .map(this::inject)
                .collect(Collectors.toList());
    }
}
