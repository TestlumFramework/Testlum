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
import com.knubisoft.testlum.testing.model.scenario.Oracle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;

@Slf4j
@InterpreterForClass(Oracle.class)
public class OracleInterpreter extends AbstractInterpreter<Oracle> {

    @Autowired(required = false)
    @Qualifier("oracleOperation")
    private AbstractStorageOperation oracleOperation;

    public OracleInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Oracle o, final CommandResult result) {
        Oracle oracle = injectCommand(o);
        String actual = getActual(oracle, result);
        CompareBuilder comparator = newCompare()
                .withActual(actual)
                .withExpected(getContentIfFile(oracle.getFile()));

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
        StorageOperationResult applyOracle = oracleOperation.apply(new ListSource(queries), alias);
        return toString(applyOracle.getRaw());
    }

}
