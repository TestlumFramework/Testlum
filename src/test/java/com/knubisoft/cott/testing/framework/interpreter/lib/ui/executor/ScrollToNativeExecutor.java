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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.ELEMENT_NOT_FOUND;

@ExecutorForClass(ScrollToNative.class)
public class ScrollToNativeExecutor extends AbstractUiExecutor<ScrollToNative> {

    private static final int DEFAULT_VALUE = -500;
    private static final int DEFAULT_SCROLLS = 20;
    private final AppiumDriver driver;

    public ScrollToNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = (AppiumDriver) dependencies.getDriver();
    }

    @Override
    public void execute(final ScrollToNative scrollToNative, final CommandResult result) {
        for (int i = 0; i < DEFAULT_SCROLLS; i++) {
            try {
                result.put("Scrolling to element with locator id", scrollToNative.getToLocatorId());
                Dimension dimension = driver.manage().window().getSize();
                Point start = new Point(dimension.width / 2, dimension.height / 2);
                driver.perform(Collections.singletonList(UiUtil.buildSequence(start, new Point(0, DEFAULT_VALUE))));
                UiUtil.findWebElement(driver, scrollToNative.getToLocatorId()).isDisplayed();
                UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
                return;
            } catch (Exception ignored) {
                //Means locator is not visible, code continue scrolling to find locator
            }
        }
        throw new DefaultFrameworkException(ELEMENT_NOT_FOUND, scrollToNative.getToLocatorId());
    }
}
