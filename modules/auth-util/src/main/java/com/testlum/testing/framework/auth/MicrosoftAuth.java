package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.model.global_config.MicrosoftAuthStrategy;

public class MicrosoftAuth extends AbstractOAuthAuth<MicrosoftAuthStrategy> {

    public MicrosoftAuth(final InterpreterDependencies dependencies, final MicrosoftAuthStrategy configuration) {
        super(dependencies, configuration, dependencies.getContext().getBean(MicrosoftTokenClient.class));
    }

    @Override
    protected String authenticationType() {
        return "MICROSOFT";
    }
}
