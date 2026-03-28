package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractDatabaseInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.model.scenario.Clickhouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Slf4j
@InterpreterForClass(Clickhouse.class)
public class ClickhouseInterpreter extends AbstractDatabaseInterpreter<Clickhouse> {

    @Autowired(required = false)
    @Qualifier("clickhouseOperation")
    private AbstractStorageOperation clickhouseOperation;

    public ClickhouseInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected AbstractStorageOperation getOperation() {
        return clickhouseOperation;
    }

    @Override
    protected String getAlias(final Clickhouse command) {
        return command.getAlias();
    }

    @Override
    protected void setAlias(final Clickhouse command, final String alias) {
        command.setAlias(alias);
    }

    @Override
    protected List<String> getQueries(final Clickhouse command) {
        return command.getQuery();
    }

    @Override
    protected String getFile(final Clickhouse command) {
        return command.getFile();
    }
}
