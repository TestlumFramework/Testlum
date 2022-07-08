package com.knubisoft.e2e.testing.framework.interpreter.lib.auth;

import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.model.scenario.Auth;


public interface AuthStrategy {
    void authenticate(Auth auth, CommandResult result);

    void logout();

}
