package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Ui;
import com.knubisoft.cott.testing.model.scenario.Web;

@InterpreterForClass(Web.class)
public class WebInterpreter extends UiInterpreter<Web>{
    public WebInterpreter(InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(Ui o, CommandResult result) {
        super.acceptImpl(o, result);
    }
}
