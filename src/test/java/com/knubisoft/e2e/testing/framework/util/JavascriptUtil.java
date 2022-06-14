package com.knubisoft.e2e.testing.framework.util;

import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.model.scenario.ScrollDirection;
import com.knubisoft.e2e.testing.model.scenario.ScrollMeasure;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_VERTICAL_PERCENT;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_VERTICAL_SCRIPT_FORMAT;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_TO_ELEMENT_NOT_SUPPORTED;
import static java.lang.String.format;

@UtilityClass
@Slf4j
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
        return format(SCROLL_VERTICAL_SCRIPT_FORMAT,scrollMeasureFormatter(measure, value));
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
}
