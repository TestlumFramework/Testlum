package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.SwipeDirection;
import com.knubisoft.cott.testing.model.scenario.SwipeNative;
import io.appium.java_client.AppiumDriver;
import java.util.Collections;
import java.util.Objects;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.PERFORM_SWIPE;

@ExecutorForClass(SwipeNative.class)
public class SwipeNativeExecutor extends AbstractUiExecutor<SwipeNative> {

    private static final int DEFAULT_VALUE = 750;
    private final AppiumDriver driver;

    public SwipeNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = (AppiumDriver) dependencies.getDriver();
    }

    @Override
    public void execute(final SwipeNative swipeNative, final CommandResult result) {
        int quantity = Objects.isNull(swipeNative.getQuantity()) ? 1 : swipeNative.getQuantity();
        for (int i = 0; i < quantity; i++) {
            result.put(PERFORM_SWIPE, swipeNative.getDirection());
            performSwipe(swipeNative);
            UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        }
    }

    private void performSwipe(final SwipeNative swipeNative) {
        Dimension dimension = driver.manage().window().getSize();
        Point start = new Point(dimension.height / 2, dimension.height / 2);
        Point end;
        if (SwipeDirection.RIGHT.equals(swipeNative.getDirection())) {
            end = new Point(start.getX() - DEFAULT_VALUE, start.getY());
        } else {
            end = new Point(start.getX() + DEFAULT_VALUE, start.getY());
        }
        driver.perform(Collections.singletonList(UiUtil.buildSequence(start, end)));
        driver.switchTo();
    }
}
