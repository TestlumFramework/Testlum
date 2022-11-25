package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.ScrollNative;
import io.appium.java_client.AppiumDriver;


@ExecutorForClass(ScrollNative.class)
public class ScrollNativeExecutor extends AbstractUiExecutor<ScrollNative> {

    private final AppiumDriver driver;

    public ScrollNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = (AppiumDriver) dependencies.getDriver();
    }

    @Override
    public void execute(final ScrollNative scrollNative, final CommandResult result) {
        Integer scrollValue = scrollNative.getValue();
        if (scrollNative.getDirection().value().equals("up")) {
            UiUtil.scrollByUnits(driver, -scrollValue);
        } else {
            UiUtil.scrollByUnits(driver, scrollValue);
        }
    }

}
