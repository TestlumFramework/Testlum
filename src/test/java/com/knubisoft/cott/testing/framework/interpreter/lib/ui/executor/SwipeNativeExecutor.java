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
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Sequence;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.AMOUNT_OF_SWIPES;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.PERFORM_SWIPE;

@ExecutorForClass(SwipeNative.class)
public class SwipeNativeExecutor extends AbstractUiExecutor<SwipeNative> {

    private static final int DEFAULT_SWIPE_VALUE = 750;
    private static final int ACTION_DURATION = 250;

    public SwipeNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final SwipeNative swipeNative, final CommandResult result) {
        int quantity = swipeNative.getQuantity();
        result.put(AMOUNT_OF_SWIPES, quantity);
        result.put(PERFORM_SWIPE, swipeNative.getDirection());
        AppiumDriver driver = (AppiumDriver) dependencies.getDriver();
        Point start = UiUtil.getCenterPoint(driver);
        for (int i = 0; i < quantity; i++) {
            performSwipe(swipeNative, driver, start);
        }
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void performSwipe(final SwipeNative swipeNative, final AppiumDriver driver, final Point start) {
        Sequence swipe = UiUtil.buildSequence(start, getEndPoint(swipeNative, start), ACTION_DURATION);
        driver.perform(Collections.singletonList(swipe));
        driver.switchTo();
    }

    private static Point getEndPoint(final SwipeNative swipeNative, final Point start) {
        return SwipeDirection.RIGHT.equals(swipeNative.getDirection())
                ? new Point(start.getX() - DEFAULT_SWIPE_VALUE, start.getY())
                : new Point(start.getX() + DEFAULT_SWIPE_VALUE, start.getY());
    }
}
