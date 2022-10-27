package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiInterpreter;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Mobilebrowser;

@InterpreterForClass(Mobilebrowser.class)
public class MobilebrowserInterpreter extends AbstractUiInterpreter<Mobilebrowser> {
    public MobilebrowserInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies,
                dependencies.getMobilebrowserDriver(),
                GlobalTestConfigurationProvider.getMobilebrowserSettings());
    }

}
