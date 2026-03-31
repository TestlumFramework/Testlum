package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractDatabaseInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.model.scenario.Dynamo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Slf4j
@InterpreterForClass(Dynamo.class)
public class DynamoDBInterpreter extends AbstractDatabaseInterpreter<Dynamo> {

    @Autowired(required = false)
    @Qualifier("dynamoOperation")
    private AbstractStorageOperation dynamoDBOperation;

    public DynamoDBInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected AbstractStorageOperation getOperation() {
        return dynamoDBOperation;
    }

    @Override
    protected String getAlias(final Dynamo command) {
        return command.getAlias();
    }

    @Override
    protected void setAlias(final Dynamo command, final String alias) {
        command.setAlias(alias);
    }

    @Override
    protected List<String> getQueries(final Dynamo command) {
        return command.getQuery();
    }

    @Override
    protected String getFile(final Dynamo command) {
        return command.getFile();
    }

    @Override
    protected String serializeResult(final Object raw) {
        return jacksonService.writeAsStringFieldVisibility(raw);
    }
}
