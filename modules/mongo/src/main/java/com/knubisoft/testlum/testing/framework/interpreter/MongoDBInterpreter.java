package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractDatabaseInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.model.scenario.Mongo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Slf4j
@InterpreterForClass(Mongo.class)
public class MongoDBInterpreter extends AbstractDatabaseInterpreter<Mongo> {

    @Autowired(required = false)
    @Qualifier("mongoOperation")
    private AbstractStorageOperation mongoOperation;

    public MongoDBInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected AbstractStorageOperation getOperation() {
        return mongoOperation;
    }

    @Override
    protected String getAlias(final Mongo command) {
        return command.getAlias();
    }

    @Override
    protected void setAlias(final Mongo command, final String alias) {
        command.setAlias(alias);
    }

    @Override
    protected List<String> getQueries(final Mongo command) {
        return command.getQuery();
    }

    @Override
    protected String getFile(final Mongo command) {
        return command.getFile();
    }
}
