package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.SwipeDirection;
import com.knubisoft.cott.testing.model.scenario.SwipeNative;
import io.appium.java_client.AppiumDriver;
import java.util.Collections;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Sequence;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SWIPE_TYPE_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.AMOUNT_OF_SWIPES;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.PERFORM_SWIPE;

@ExecutorForClass(SwipeNative.class)
public class SwipeNativeExecutor extends AbstractUiExecutor<SwipeNative> {

    private static final int DEFAULT_SWIPE_VALUE = 1000;
    private static final int ACTION_DURATION = 250;

    public SwipeNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final SwipeNative swipeNative, final CommandResult result) {
        result.put(AMOUNT_OF_SWIPES, swipeNative.getQuantity());
        result.put(PERFORM_SWIPE, swipeNative.getDirection());
        LogUtil.logSwipeNativeInfo(swipeNative);
        performSwipe(swipeNative);
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void performSwipe(final SwipeNative swipeNative) {
        AppiumDriver driver = (AppiumDriver) dependencies.getDriver();
        Point start = UiUtil.getCenterPoint(driver);
        Sequence swipe = UiUtil.buildSequence(start, getEndPoint(swipeNative.getDirection(), start), ACTION_DURATION);
        for (int i = 0; i < swipeNative.getQuantity(); i++) {
            driver.perform(Collections.singletonList(swipe));
            driver.switchTo();
        }
    }

    private Point getEndPoint(final SwipeDirection direction, final Point start) {
        switch (direction) {
            case UP:
                return new Point(start.getX(), start.getY() - DEFAULT_SWIPE_VALUE);
            case DOWN:
                return new Point(start.getX(), start.getY() + DEFAULT_SWIPE_VALUE);
            case LEFT:
                return new Point(start.getX() + DEFAULT_SWIPE_VALUE, start.getY());
            case RIGHT:
                return new Point(start.getX() - DEFAULT_SWIPE_VALUE, start.getY());
            default:
                throw new DefaultFrameworkException(SWIPE_TYPE_NOT_FOUND, direction);
        }
    }
}
