package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.ExceptionMessage;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.scenario.Scroll;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.JS_FILE_UNREADABLE;
import static java.lang.String.format;

@UtilityClass

public class JavascriptUtil {

    public void executeJsScript(final WebElement element, final String script, final WebDriver driver) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        javascriptExecutor.executeScript(script, element);
    }

    public void executeJsScript(final String script, final WebDriver driver) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        javascriptExecutor.executeScript(script);
    }

    public String readCommands(final String filePath) {
        try {
            File jsFile = FileSearcher.searchFileFromDataFolder(filePath);
            List<String> commands = Files.readAllLines(jsFile.toPath());
            return String.join(EMPTY, commands);
        } catch (IOException e) {
            throw new DefaultFrameworkException(format(JS_FILE_UNREADABLE, filePath));
        }
    }

    public static void executeScrollScript(final Scroll scroll, final WebDriver webDriver) {
        switch (scroll.getType()) {
            case INNER:
                JavascriptUtil.executeJsScript(InnerScrollScript.getInnerScrollScript(scroll), webDriver);
                break;
            case PAGE:
                JavascriptUtil.executeJsScript(PageScrollScript.getPageScrollScript(scroll), webDriver);
                break;
            default:
                throw new DefaultFrameworkException(format(ExceptionMessage.SCROLL_TYPE_NOT_FOUND, scroll.getType()));
        }
    }
}
