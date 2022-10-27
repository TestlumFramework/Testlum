package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.executor.ExecutorHolder;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model._native.scenario.NewNative;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;

import java.util.Map;

@InterpreterForClass(NewNative.class)
public class NewNativeInterpreter extends AbstractInterpreter<NewNative> {

    public NewNativeInterpreter(InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(NewNative o, CommandResult result) {
        for (AbstractCommand command : o.getCommandOneOrCommandTwo()) {
            ExecutorHolder.getAppropriateExecutor(command).execute(command, dependencies.getWebDriver(), result);
            //нужно еще хендлить ошибку если нет нужного executor
        }
    }
}
