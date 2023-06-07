package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.db.sql.ClickhouseOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Clickhouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
@InterpreterForClass(Clickhouse.class)
public class ClickhouseInterpreter extends AbstractInterpreter<Clickhouse> {

    @Autowired(required = false)
    private ClickhouseOperation clickhouseOperation;

    public ClickhouseInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Clickhouse o, final CommandResult result) {
        Clickhouse clickhouse = injectCommand(o);
        String actual = getActual(clickhouse, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpectedFile(clickhouse.getFile());

        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));
        result.setActual(StringPrettifier.asJsonResult(actual));

        comparator.exec();
        setContextBody(actual);
    }

    protected String getActual(final Clickhouse clickhouse, final CommandResult result) {
        String alias = clickhouse.getAlias();
        List<String> queries = clickhouse.getQuery();
        LogUtil.logAllQueries(queries, alias);
        ResultUtil.addDatabaseMetaData(alias, queries, result);
        StorageOperation.StorageOperationResult applyClickhouse = clickhouseOperation.apply(
                new ListSource(queries), alias);
        return toString(applyClickhouse.getRaw());
    }
}
