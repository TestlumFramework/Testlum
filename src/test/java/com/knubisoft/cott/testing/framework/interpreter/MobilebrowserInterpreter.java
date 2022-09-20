package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Mobilebrowser;
import com.knubisoft.cott.testing.model.scenario.Ui;

@InterpreterForClass(Mobilebrowser.class)
public class MobilebrowserInterpreter extends UiInterpreter<Mobilebrowser>{
    public MobilebrowserInterpreter(InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(Ui o, CommandResult result) {
        super.acceptImpl(o, result);
    }
}
