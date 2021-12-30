package com.knubisoft.e2e.testing.framework.util;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebElement;

import java.io.File;

@UtilityClass
public final class SeleniumUtil {
    private static final String FILE_PATH_PREFIX = "file:";

    public String resolveSendKeysType(final String value, final FileSearcher searcher, final WebElement element) {
        if (value.startsWith(FILE_PATH_PREFIX)) {
            File file = searcher.search(value.substring(FILE_PATH_PREFIX.length()));
            return file.getPath();
        }
        element.clear();
        return value;
    }
}
