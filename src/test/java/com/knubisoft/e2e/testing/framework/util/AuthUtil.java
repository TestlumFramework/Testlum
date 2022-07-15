package com.knubisoft.e2e.testing.framework.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.nio.charset.StandardCharsets;

@UtilityClass
public class AuthUtil {
    @SneakyThrows
    public String getCredentialsFromFile(final FileSearcher fileSearcher, final String fileName) {
        return FileUtils.readFileToString(fileSearcher.fileByNameAndExtension(fileName), StandardCharsets.UTF_8);
    }
}
