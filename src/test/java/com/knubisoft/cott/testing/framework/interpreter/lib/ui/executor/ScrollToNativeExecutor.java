package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.ScrollToNative;
import io.appium.java_client.AppiumDriver;
import java.util.Collections;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Sequence;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.ELEMENT_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.SCROLL_TO_ELEMENT;

@ExecutorForClass(ScrollToNative.class)
public class ScrollToNativeExecutor extends AbstractUiExecutor<ScrollToNative> {

    private static final int DEFAULT_SCROLL_VALUE = -250;
    private static final int DEFAULT_SCROLLS_COUNT = 20;
    private static final int ACTION_DURATION = 750;

    public ScrollToNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final ScrollToNative scrollToNative, final CommandResult result) {
        AppiumDriver driver = (AppiumDriver) dependencies.getDriver();
        Point start = UiUtil.getCenterPoint(driver);
        Point end = new Point(0, DEFAULT_SCROLL_VALUE);
        Sequence scroll = UiUtil.buildSequence(start, end, ACTION_DURATION);
        result.put(SCROLL_TO_ELEMENT, scrollToNative.getLocatorId());
        processScrollToElement(scrollToNative, result, driver, scroll);
    }

    private void processScrollToElement(final ScrollToNative scrollToNative,
                                        final CommandResult result,
                                        final AppiumDriver driver,
                                        final Sequence scroll) {
        for (int i = 0; i < DEFAULT_SCROLLS_COUNT; i++) {
            try {
                driver.perform(Collections.singletonList(scroll));
                UiUtil.findWebElement(driver, scrollToNative.getLocatorId()).isDisplayed();
                UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
                return;
            } catch (NoSuchElementException e) {
                //Means locator is not visible, code continue scrolling to find locator
            }
        }
        throw new DefaultFrameworkException(ELEMENT_NOT_FOUND, scrollToNative.getLocatorId());
    }
}
