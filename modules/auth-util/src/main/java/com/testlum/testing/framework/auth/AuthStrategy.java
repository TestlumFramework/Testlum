package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Auth;

public interface AuthStrategy {

    void authenticate(Auth auth, CommandResult result);

    void logout();
}
