package com.knubisoft.testlum.testing.framework.interpreter.lib.auth;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Auth;

public interface AuthStrategy {

    void authenticate(Auth auth, CommandResult result);

    void logout();
}
