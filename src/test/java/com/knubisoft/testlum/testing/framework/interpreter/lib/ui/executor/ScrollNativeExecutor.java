package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.ScrollDirection;
import com.knubisoft.testlum.testing.model.scenario.ScrollNative;
import com.knubisoft.testlum.testing.model.scenario.ScrollType;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Sequence;

import java.util.Collections;

@ExecutorForClass(ScrollNative.class)
public class ScrollNativeExecutor extends AbstractUiExecutor<ScrollNative> {

    private static final int ACTION_DURATION = 700;

    public ScrollNativeExecutor(final GlobalTestConfigurationProvider configurationProvider,
                                final ExecutorDependencies dependencies) {
        super(configurationProvider, dependencies);
    }

    @Override
    public void execute(final ScrollNative scrollNative, final CommandResult result) {
        AppiumDriver driver = (AppiumDriver) dependencies.getDriver();
        int scrollValue = ScrollDirection.UP == scrollNative.getDirection()
                ? scrollNative.getValue()
                : -scrollNative.getValue();
        ResultUtil.addScrollNativeMetaDada(scrollNative, result);
        LogUtil.logScrollNativeInfo(scrollNative);
        Point start = getStartPoint(scrollNative, driver);
        Sequence scroll = UiUtil.buildSequence(start, new Point(0, scrollValue), ACTION_DURATION);
        driver.perform(Collections.singletonList(scroll));
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private Point getStartPoint(final ScrollNative scrollNative, final AppiumDriver driver) {
        return ScrollType.INNER == scrollNative.getType()
                ? UiUtil.findWebElement(dependencies, scrollNative.getLocatorId()).getLocation()
                : UiUtil.getCenterPoint(driver);
    }

}
