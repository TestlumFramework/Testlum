package com.knubisoft.cott.testing.framework.interpreter.lib.auth;

import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Auth;

public interface AuthStrategy {

    void authenticate(Auth auth, CommandResult result);

    void logout();
}
