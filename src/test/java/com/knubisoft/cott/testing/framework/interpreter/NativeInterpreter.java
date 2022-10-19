package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiInterpreter;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Native;
import com.knubisoft.cott.testing.model.scenario.Ui;

@InterpreterForClass(Native.class)
public class NativeInterpreter extends AbstractUiInterpreter<Native> {
    public NativeInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies, dependencies.getNativeDriver(), GlobalTestConfigurationProvider.getNativeSettings());
    }

    @Override
    protected void acceptImpl(final Ui o, final CommandResult result) {
        super.acceptImpl(o, result);
    }


}
