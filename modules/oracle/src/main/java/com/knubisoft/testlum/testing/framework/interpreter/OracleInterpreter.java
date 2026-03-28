package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractDatabaseInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.model.scenario.Oracle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Slf4j
@InterpreterForClass(Oracle.class)
public class OracleInterpreter extends AbstractDatabaseInterpreter<Oracle> {

    @Autowired(required = false)
    @Qualifier("oracleOperation")
    private AbstractStorageOperation oracleOperation;

    public OracleInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected AbstractStorageOperation getOperation() {
        return oracleOperation;
    }

    @Override
    protected String getAlias(final Oracle command) {
        return command.getAlias();
    }

    @Override
    protected void setAlias(final Oracle command, final String alias) {
        command.setAlias(alias);
    }

    @Override
    protected List<String> getQueries(final Oracle command) {
        return command.getQuery();
    }

    @Override
    protected String getFile(final Oracle command) {
        return command.getFile();
    }
}
