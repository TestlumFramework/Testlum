package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Native;
import com.knubisoft.cott.testing.model.scenario.Navigate;
import com.knubisoft.cott.testing.model.scenario.Ui;

@InterpreterForClass(Navigate.class)
public class NativeInterpreter extends UiInterpreter<Native>{
    public NativeInterpreter(InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(Ui o, CommandResult result) {
        super.acceptImpl(o, result);
    }
}
