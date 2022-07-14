package com.knubisoft.e2e.testing.framework.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthUtil {
    public String getCredentialsFromFile(final FileSearcher fileSearcher, final String fileName) {
        return fileSearcher.searchFileAndReadToString(fileName);
    }
}
