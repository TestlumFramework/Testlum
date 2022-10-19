package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractCommonWebInterpreter;
import com.knubisoft.cott.testing.model.scenario.Web;

@InterpreterForClass(Web.class)
public class WebInterpreter extends AbstractCommonWebInterpreter<Web> {
    public WebInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies, dependencies.getWebDriver(), GlobalTestConfigurationProvider.getBrowserSettings());
    }
}
