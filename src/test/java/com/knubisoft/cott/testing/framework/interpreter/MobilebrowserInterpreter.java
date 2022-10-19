package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractCommonWebInterpreter;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Mobilebrowser;
import com.knubisoft.cott.testing.model.scenario.Ui;

@InterpreterForClass(Mobilebrowser.class)
public class MobilebrowserInterpreter extends AbstractCommonWebInterpreter<Mobilebrowser> {
    public MobilebrowserInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies,
                dependencies.getMobilebrowserDriver(),
                GlobalTestConfigurationProvider.getMobilebrowserSettings());
    }

    @Override
    protected void acceptImpl(final Ui ui, final CommandResult result) {
        super.acceptImpl(ui, result);
    }

}
