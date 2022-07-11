package com.knubisoft.e2e.testing.framework.util;

import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class AuthUtil {
    public String getCredentialsFromFile(final FileSearcher fileSearcher, final String fileName) {
        File credentialsFolder = TestResourceSettings.getInstance().getCredentialsFolder();
        return fileSearcher
                .searchFileAndReadToString(credentialsFolder, fileName);
    }
}
