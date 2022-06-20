package com.knubisoft.e2e.testing.framework.util;

import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.model.scenario.ScrollDirection;
import com.knubisoft.e2e.testing.model.scenario.ScrollMeasure;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_VERTICAL_PERCENT;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_VERTICAL_SCRIPT_FORMAT;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.JS_FILE_NOT_FOUND;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.JS_FILE_UNREADABLE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_TO_ELEMENT_NOT_SUPPORTED;
import static java.lang.String.format;

@UtilityClass

public class JavascriptUtil {
    private static final int MAX_PERCENTS_VALUE = 100;

    public void executeJsScript(final WebElement element, final String script, final WebDriver driver) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        javascriptExecutor.executeScript(script, element);
    }

    public void executeJsScript(final String script, final WebDriver driver) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        javascriptExecutor.executeScript(script);
    }

    public String getScrollScript(final ScrollDirection direction,
                                  final String value, final ScrollMeasure measure) {
        LogUtil.logScrollInfo(direction.name(), measure.value(), value);
        if (direction.equals(ScrollDirection.UP)) {
            return format(SCROLL_VERTICAL_SCRIPT_FORMAT,
                    scrollMeasureFormatter(measure, DelimiterConstant.DASH + value));
        }
        return format(SCROLL_VERTICAL_SCRIPT_FORMAT, scrollMeasureFormatter(measure, value));
    }

    private String scrollMeasureFormatter(final ScrollMeasure measure, final String value) {
        if (measure.equals(ScrollMeasure.PERCENT)) {
            float percent = Float.parseFloat(value) / MAX_PERCENTS_VALUE;
            if (percent > 1) {
                throw new DefaultFrameworkException(format(SCROLL_TO_ELEMENT_NOT_SUPPORTED, value));
            }
            return format(SCROLL_VERTICAL_PERCENT, percent);
        }
        return value;
    }

    public String readCommands(final String filePath, final URL resource, final FileSearcher fileSearcher) {
        try {
            File jsFile = getJsFileByPath(filePath, resource, fileSearcher);
            List<String> commands = Files.readAllLines(jsFile.toPath());
            return String.join(EMPTY, commands);
        } catch (IOException e) {
            throw new DefaultFrameworkException(format(JS_FILE_UNREADABLE, filePath));
        }
    }

    private File getJsFileByPath(final String filePath, final URL resource, final FileSearcher fileSearcher) {
        try {
            File fromDir = new File(Objects.requireNonNull(resource).toURI());
            return fileSearcher.search(fromDir, filePath);
        } catch (URISyntaxException e) {
            throw new DefaultFrameworkException(format(JS_FILE_NOT_FOUND, filePath));
        }
    }
}
