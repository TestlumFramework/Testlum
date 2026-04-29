package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JavascriptUtil {

    private final FileSearcher fileSearcher;

    public Object executeJsScript(final String script, final WebDriver driver, final Object... arg) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        return javascriptExecutor.executeScript(script, arg);
    }

    public String readCommands(final String filePath) {
        try {
            File jsFile = fileSearcher.searchFileFromDataFolder(filePath);
            List<String> commands = Files.readAllLines(jsFile.toPath());
            return String.join(DelimiterConstant.EMPTY, commands);
        } catch (IOException e) {
            throw new DefaultFrameworkException(ExceptionMessage.JS_FILE_UNREADABLE, filePath);
        }
    }
}
