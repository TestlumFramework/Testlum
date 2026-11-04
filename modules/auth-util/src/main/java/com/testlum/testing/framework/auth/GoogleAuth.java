package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.model.global_config.GoogleAuthStrategy;

public class GoogleAuth extends AbstractOAuthAuth<GoogleAuthStrategy> {

    public GoogleAuth(final InterpreterDependencies dependencies, final GoogleAuthStrategy configuration) {
        super(dependencies, configuration, dependencies.getContext().getBean(GoogleTokenClient.class));
    }

    @Override
    protected String authenticationType() {
        return "GOOGLE";
    }
}
