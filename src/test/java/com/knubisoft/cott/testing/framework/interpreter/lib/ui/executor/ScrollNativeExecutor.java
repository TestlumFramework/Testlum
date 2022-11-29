package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.ScrollDirection;
import com.knubisoft.cott.testing.model.scenario.ScrollNative;
import com.knubisoft.cott.testing.model.scenario.ScrollType;
import io.appium.java_client.AppiumDriver;
import java.util.Collections;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;


@ExecutorForClass(ScrollNative.class)
public class ScrollNativeExecutor extends AbstractUiExecutor<ScrollNative> {

    private AppiumDriver driver;

    public ScrollNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = (AppiumDriver) dependencies.getDriver();
    }

    @Override
    public void execute(final ScrollNative scrollNative, final CommandResult result) {
        int scrollValue = ScrollDirection.UP.equals(scrollNative.getDirection()) ? scrollNative.getValue()
                : -scrollNative.getValue();
        result.put("Scroll direction", scrollNative.getDirection().value());
        result.put("Scroll value", Math.abs(scrollValue));
        Point start;
        if (ScrollType.INNER.equals(scrollNative.getType())) {
            start = UiUtil.findWebElement(driver, scrollNative.getLocator()).getLocation();
        } else {
            Dimension dimension = driver.manage().window().getSize();
            start = new Point(dimension.width / 2, dimension.height / 2);
        }
        driver.perform(Collections.singletonList(UiUtil.buildSequence(start, new Point(0, scrollValue))));
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

}
