package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.ScrollToNative;
import io.appium.java_client.AppiumDriver;

@ExecutorForClass(ScrollToNative.class)
public class ScrollToNativeExecutor extends AbstractUiExecutor<ScrollToNative> {

    private final AppiumDriver driver;
    private final Integer defaultValue = 500;
    private final Integer defaultScrolls = 20;

    public ScrollToNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = (AppiumDriver) dependencies.getDriver();
    }

    @Override
    public void execute(final ScrollToNative scrollToNative, final CommandResult result) {
        for (int i = 0; i < defaultScrolls; i++) {
            try {
                UiUtil.scrollByUnits(driver, defaultValue);
                UiUtil.findWebElement(driver, scrollToNative.getToLocatorId());
                break;
            } catch (Exception ignored) {
                //Means locator is not visible, code continue scrolling to find locator
            }
        }
    }
}
