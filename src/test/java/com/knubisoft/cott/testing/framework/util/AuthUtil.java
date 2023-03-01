package com.knubisoft.cott.testing.framework.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.nio.charset.StandardCharsets;

@UtilityClass
public class AuthUtil {
    @SneakyThrows
    public String getCredentialsFromFile(final String fileName) {
        return FileUtils.readFileToString(FileSearcher.searchFileFromDataFolder(fileName), StandardCharsets.UTF_8);
    }
}
