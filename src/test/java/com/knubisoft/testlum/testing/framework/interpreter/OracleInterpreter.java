package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.db.sql.OracleOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.scenario.Oracle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
@InterpreterForClass(Oracle.class)
public class OracleInterpreter extends AbstractInterpreter<Oracle> {

    @Autowired(required = false)
    private OracleOperation oracleOperation;

    public OracleInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Oracle o, final CommandResult result) {
        Oracle oracle = injectCommand(o);
        String actual = getActual(oracle, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpectedFile(oracle.getFile());

        result.setActual(StringPrettifier.asJsonResult(actual));
        result.setExpected(StringPrettifier.asJsonResult(comparator.getExpected()));

        comparator.exec();
        setContextBody(actual);
    }

    protected String getActual(final Oracle oracle, final CommandResult result) {
        String alias = oracle.getAlias();
        List<String> queries = oracle.getQuery();
        LogUtil.logAllQueries(queries, oracle.getAlias());
        ResultUtil.addDatabaseMetaData(alias, queries, result);
        StorageOperation.StorageOperationResult applyOracle = oracleOperation.apply(new ListSource(queries), alias);
        return toString(applyOracle.getRaw());
    }

}
