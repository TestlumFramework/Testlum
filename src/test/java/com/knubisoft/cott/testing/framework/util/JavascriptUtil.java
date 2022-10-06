package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.scenario.ScrollDirection;
import com.knubisoft.cott.testing.model.scenario.ScrollMeasure;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.INNER_SCROLL_VERTICAL_PERCENT;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.INNER_SCROLL_VERTICAL_SCRIPT_FORMAT;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.JS_FILE_UNREADABLE;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SCROLL_TO_ELEMENT_NOT_SUPPORTED;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.PAGE_SCROLL_VERTICAL_PERCENT;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.PAGE_SCROLL_VERTICAL_SCRIPT_FORMAT;
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

    public String getPageScrollScript(final ScrollDirection direction,
                                  final String value, final ScrollMeasure measure) {
        LogUtil.logScrollInfo(direction.name(), measure.value(), value);
        if (direction.equals(ScrollDirection.UP)) {
            return format(PAGE_SCROLL_VERTICAL_SCRIPT_FORMAT,
                    pageScrollMeasureFormatter(measure, DelimiterConstant.DASH + value));
        }
        return format(PAGE_SCROLL_VERTICAL_SCRIPT_FORMAT, pageScrollMeasureFormatter(measure, value));
    }

    public String getInnerScrollScript(final ScrollDirection direction,
                                  final String value, final ScrollMeasure measure, final String selector) {
        LogUtil.logInnerScrollInfo(direction.name(), measure.value(), value, selector);
        if (direction.equals(ScrollDirection.UP)) {
            return format(INNER_SCROLL_VERTICAL_SCRIPT_FORMAT, selector,
                    innerScrollMeasureFormatter(measure, DelimiterConstant.DASH + value, selector));
        }
        return format(INNER_SCROLL_VERTICAL_SCRIPT_FORMAT, selector,
                innerScrollMeasureFormatter(measure, value, selector));
    }

    private String innerScrollMeasureFormatter(final ScrollMeasure measure, final String value, final String selector) {
        if (measure.equals(ScrollMeasure.PERCENT)) {
            float percent = Float.parseFloat(value) / MAX_PERCENTS_VALUE;
            if (percent > 1) {
                throw new DefaultFrameworkException(format(SCROLL_TO_ELEMENT_NOT_SUPPORTED, value));
            }
            return format(INNER_SCROLL_VERTICAL_PERCENT, selector, percent);
        }
        return value;
    }

    private String pageScrollMeasureFormatter(final ScrollMeasure measure, final String value) {
        if (measure.equals(ScrollMeasure.PERCENT)) {
            float percent = Float.parseFloat(value) / MAX_PERCENTS_VALUE;
            if (percent > 1) {
                throw new DefaultFrameworkException(format(SCROLL_TO_ELEMENT_NOT_SUPPORTED, value));
            }
            return format(PAGE_SCROLL_VERTICAL_PERCENT, percent);
        }
        return value;
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
}
