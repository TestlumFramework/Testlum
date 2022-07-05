package com.knubisoft.e2e.testing.framework.interpreter.lib.auth;

import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.model.scenario.Auth;

public interface AuthStrategy {
    void login(InterpreterDependencies dependencies, Auth auth);
}
