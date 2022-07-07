package com.knubisoft.e2e.testing.framework.interpreter.lib.auth;

import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
import com.knubisoft.e2e.testing.model.scenario.Auth;

import java.io.File;

public interface AuthStrategy {
    void authenticate(InterpreterDependencies dependencies, Auth auth, CommandResult result);

    void logout(InterpreterDependencies dependencies);

    default String getCredentialsFromFile(final FileSearcher fileSearcher, final String fileName) {
        File credentialsFolder = TestResourceSettings.getInstance().getCredentialsFolder();
        return fileSearcher
                .searchFileAndReadToString(credentialsFolder, fileName);
    }
}
