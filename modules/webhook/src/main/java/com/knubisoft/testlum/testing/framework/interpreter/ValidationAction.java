package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.http.HttpValidator;
import com.knubisoft.testlum.testing.framework.report.CommandResult;

@FunctionalInterface
public interface ValidationAction {

    void validate(HttpValidator httpValidator, CommandResult subResult) throws Exception;

}
