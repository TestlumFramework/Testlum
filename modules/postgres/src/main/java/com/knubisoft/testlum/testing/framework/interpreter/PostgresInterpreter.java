package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractDatabaseInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.model.scenario.Postgres;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Slf4j
@InterpreterForClass(Postgres.class)
public class PostgresInterpreter extends AbstractDatabaseInterpreter<Postgres> {

    @Autowired(required = false)
    @Qualifier("postgresOperation")
    private AbstractStorageOperation postgresSqlOperation;

    public PostgresInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected AbstractStorageOperation getOperation() {
        return postgresSqlOperation;
    }

    @Override
    protected String getAlias(final Postgres command) {
        return command.getAlias();
    }

    @Override
    protected void setAlias(final Postgres command, final String alias) {
        command.setAlias(alias);
    }

    @Override
    protected List<String> getQueries(final Postgres command) {
        return command.getQuery();
    }

    @Override
    protected String getFile(final Postgres command) {
        return command.getFile();
    }
}
