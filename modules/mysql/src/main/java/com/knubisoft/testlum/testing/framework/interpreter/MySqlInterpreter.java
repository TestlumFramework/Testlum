package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractDatabaseInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.model.scenario.Mysql;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Slf4j
@InterpreterForClass(Mysql.class)
public class MySqlInterpreter extends AbstractDatabaseInterpreter<Mysql> {

    @Autowired(required = false)
    @Qualifier("mySqlOperation")
    private AbstractStorageOperation mySqlOperation;

    public MySqlInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected AbstractStorageOperation getOperation() {
        return mySqlOperation;
    }

    @Override
    protected String getAlias(final Mysql command) {
        return command.getAlias();
    }

    @Override
    protected void setAlias(final Mysql command, final String alias) {
        command.setAlias(alias);
    }

    @Override
    protected List<String> getQueries(final Mysql command) {
        return command.getQuery();
    }

    @Override
    protected String getFile(final Mysql command) {
        return command.getFile();
    }
}
