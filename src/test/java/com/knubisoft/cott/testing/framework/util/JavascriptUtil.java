package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.constant.ExceptionMessage;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.UiInterpreter;
import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.ScrollDirection;
import com.knubisoft.cott.testing.model.scenario.ScrollMeasure;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.INNER_SCROLL_VERTICAL_PERCENT_BY_CLASS;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.INNER_SCROLL_VERTICAL_PERCENT_BY_CSS_SELECTOR;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.JS_FILE_UNREADABLE;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SCROLL_TO_ELEMENT_NOT_SUPPORTED;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.INNER_SCROLL_VERTICAL_PERCENT_BY_ID;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.INNER_SCROLL_VERTICAL_PERCENT_BY_XPATH;
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

    public String getPageScrollScript(
            final ScrollDirection direction,
            final String value,
            final ScrollMeasure measure) {
        LogUtil.logScrollInfo(direction.name(), measure.value(), value);
        if (direction.equals(ScrollDirection.UP)) {
            return format(PAGE_SCROLL_VERTICAL_SCRIPT_FORMAT,
                    pageScrollMeasureFormatter(measure, DelimiterConstant.DASH + value));
        }
        return format(PAGE_SCROLL_VERTICAL_SCRIPT_FORMAT, pageScrollMeasureFormatter(measure, value));
    }

    private String findScriptByLocator(final Locator locator) {
        return UiInterpreter.SCRIPT_TYPE.entrySet().stream()
                .filter(l -> l.getKey().test(locator))
                .findFirst()
                .map(Map.Entry::getValue)
                .map(Pair::getRight)
                .orElseThrow(() -> new DefaultFrameworkException(
                        format(ExceptionMessage.NO_SUCH_SCRIPT_FOR_THIS_LOCATOR, locator)));
    }

    private String findLocator(final Locator locator) {
        return UiInterpreter.SCRIPT_TYPE.entrySet().stream()
                .filter(l -> l.getKey().test(locator))
                .findFirst()
                .map(Map.Entry::getValue)
                .map(Pair::getLeft)
                .map(l -> l.apply(locator))
                .orElseThrow(() -> new DefaultFrameworkException(
                        format(ExceptionMessage.NO_SUCH_LOCATOR, locator)));
    }
    public String getInnerScrollScript(
            final ScrollDirection direction,
            final String value,
            final ScrollMeasure measure,
            final Locator locator,
            final String scrollLocator) {
        String selector = findLocator(GlobalLocators.getLocator(scrollLocator));
        LogUtil.logInnerScrollInfo(direction.name(), measure.value(), value, selector);
        if (ScrollDirection.UP.equals(direction)) {
            return format(findScriptByLocator(locator), selector,
                    innerScrollMeasureFormatter(measure, DelimiterConstant.DASH + value, selector, locator));
        }
        return format(findScriptByLocator(locator), selector,
                innerScrollMeasureFormatter(measure, value, selector, locator));
    }

    private String findPercentScrollScriptByLocator(final Locator locator) {
        if (Objects.nonNull(locator.getCssSelector())) {
            return INNER_SCROLL_VERTICAL_PERCENT_BY_CSS_SELECTOR;
        } else if (Objects.nonNull(locator.getId())) {
            return INNER_SCROLL_VERTICAL_PERCENT_BY_ID;
        } else if (Objects.nonNull(locator.getClazz())) {
            return INNER_SCROLL_VERTICAL_PERCENT_BY_CLASS;
        } else if (Objects.nonNull(locator.getXpath())) {
            return INNER_SCROLL_VERTICAL_PERCENT_BY_XPATH;
        }
        throw new DefaultFrameworkException(format(ExceptionMessage.NO_SUCH_SCRIPT_FOR_THIS_LOCATOR, locator));
    }

    private String innerScrollMeasureFormatter(final ScrollMeasure measure,
                                               final String value,
                                               final String selector,
                                               final Locator locator) {
        if (ScrollMeasure.PERCENT.equals(measure)) {
            float percent = Float.parseFloat(value) / MAX_PERCENTS_VALUE;
            if (percent > 1) {
                throw new DefaultFrameworkException(format(SCROLL_TO_ELEMENT_NOT_SUPPORTED, value));
            }
            return format(findPercentScrollScriptByLocator(locator), selector, percent);
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
