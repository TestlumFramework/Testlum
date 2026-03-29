package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractDatabaseInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.model.scenario.SqlDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Slf4j
@InterpreterForClass(SqlDatabase.class)
public class SqlDatabaseInterpreter extends AbstractDatabaseInterpreter<SqlDatabase> {

    @Autowired(required = false)
    @Qualifier("sqlDatabaseOperation")
    private AbstractStorageOperation sqlDatabaseOperation;

    public SqlDatabaseInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected AbstractStorageOperation getOperation() {
        return sqlDatabaseOperation;
    }

    @Override
    protected String getAlias(final SqlDatabase command) {
        return command.getAlias();
    }

    @Override
    protected void setAlias(final SqlDatabase command, final String alias) {
        command.setAlias(alias);
    }

    @Override
    protected List<String> getQueries(final SqlDatabase command) {
        return command.getQuery();
    }

    @Override
    protected String getFile(final SqlDatabase command) {
        return command.getFile();
    }
}
