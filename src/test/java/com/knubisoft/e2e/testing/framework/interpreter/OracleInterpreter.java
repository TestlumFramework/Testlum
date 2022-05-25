package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.db.sql.OracleOperation;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.model.scenario.Oracle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.ALIAS_LOG;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
@InterpreterForClass(Oracle.class)
public class OracleInterpreter extends AbstractInterpreter<Oracle> {

    private static final String SQL_LOG_TEMPLATE = "%10s %-100s";

    @Autowired(required = false)
    private OracleOperation oracleOperation;

    public OracleInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Oracle oracle, final CommandResult result) {
        String actual = getActual(oracle, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpectedFile(oracle.getFile());

        result.setActual(actual);
        result.setExpected(comparator.getExpected());

        comparator.exec();
        setContextBody(actual);
    }

    protected String getActual(final Oracle oracle, final CommandResult result) {
        log.info(ALIAS_LOG, oracle.getAlias());
        List<String> sqls = getSqlList(oracle);
        result.put("sqls", sqls);
        StorageOperation.StorageOperationResult applyOracle = oracleOperation.apply(new ListSource(sqls),
                inject(oracle.getAlias()));
        return toString(applyOracle.getRaw());
    }

    private List<String> getSqlList(final Oracle oracle) {
        List<String> queriesOracle = oracle.getQuery().stream()
                .map(this::inject)
                .collect(Collectors.toList());

        queriesOracle.forEach(it -> log.info(format(SQL_LOG_TEMPLATE, EMPTY, it)));
        return queriesOracle;
    }
}
