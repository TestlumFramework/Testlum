package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.Refresh;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Sequence;

import java.util.Collections;

@ExecutorForClass(Refresh.class)
public class RefreshExecutor extends AbstractUiExecutor<Refresh> {

    private static final int ACTION_DURATION = 250;
    private static final int DEFAULT_REFRESH_VALUE = 1500;
    private static final int DEFAULT_SCREEN_SEGMENT = 5;

    public RefreshExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Refresh refresh, final CommandResult result) {
        AppiumDriver driver = (AppiumDriver) dependencies.getDriver();
        Dimension dimensions = driver.manage().window().getSize();
        Point start = new Point(dimensions.width / 2, dimensions.height / DEFAULT_SCREEN_SEGMENT);
        Sequence preRefreshAction =
                UiUtil.buildSequence(start, new Point(start.x, -1), ACTION_DURATION);
        Sequence refreshAction =
                UiUtil.buildSequence(start, new Point(start.x, DEFAULT_REFRESH_VALUE), ACTION_DURATION);
        driver.perform(Collections.singletonList(preRefreshAction));
        driver.perform(Collections.singletonList(refreshAction));
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
