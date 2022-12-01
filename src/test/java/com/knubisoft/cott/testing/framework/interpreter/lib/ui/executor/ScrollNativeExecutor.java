package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.ScrollDirection;
import com.knubisoft.cott.testing.model.scenario.ScrollNative;
import com.knubisoft.cott.testing.model.scenario.ScrollType;
import io.appium.java_client.AppiumDriver;
import java.util.Collections;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Sequence;


@ExecutorForClass(ScrollNative.class)
public class ScrollNativeExecutor extends AbstractUiExecutor<ScrollNative> {

    private static final int ACTION_DURATION = 700;

    public ScrollNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final ScrollNative scrollNative, final CommandResult result) {
        AppiumDriver driver = (AppiumDriver) dependencies.getDriver();
        int scrollValue = ScrollDirection.UP.equals(scrollNative.getDirection())
                ? scrollNative.getValue()
                : -scrollNative.getValue();
        ResultUtil.addScrollNativeMetaDada(scrollNative, result);
        Point start = getStartPoint(scrollNative, driver);
        Sequence scroll = UiUtil.buildSequence(start, new Point(0, scrollValue), ACTION_DURATION);
        driver.perform(Collections.singletonList(scroll));
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private static Point getStartPoint(final ScrollNative scrollNative, final AppiumDriver driver) {
        return ScrollType.INNER.equals(scrollNative.getType())
                ? UiUtil.findWebElement(driver, scrollNative.getLocator()).getLocation()
                : UiUtil.getCenterPoint(driver);
    }

}
