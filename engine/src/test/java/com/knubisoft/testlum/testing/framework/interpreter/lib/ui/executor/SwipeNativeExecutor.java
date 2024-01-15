package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;

import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.SwipeDirection;
import com.knubisoft.testlum.testing.model.scenario.SwipeNative;
import com.knubisoft.testlum.testing.model.scenario.SwipeType;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Sequence;

import java.util.Collections;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SWIPE_TYPE_NOT_FOUND;

@ExecutorForClass(SwipeNative.class)
public class SwipeNativeExecutor extends AbstractUiExecutor<SwipeNative> {
    private static final int ACTION_DURATION = 250;
    private static final int PERCENTS = 100;

    public SwipeNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final SwipeNative swipeNative, final CommandResult result) {
        ResultUtil.addSwipeMetaData(swipeNative, result);
        LogUtil.logSwipeNativeInfo(swipeNative);
        performSwipe(swipeNative);
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void performSwipe(final SwipeNative swipeNative) {
        AppiumDriver driver = (AppiumDriver) dependencies.getDriver();
        Sequence swipe = prepareSwipe(swipeNative, driver);
        for (int i = 0; i < swipeNative.getQuantity(); i++) {
            driver.perform(Collections.singletonList(swipe));
            driver.switchTo();
        }
    }

    private Sequence prepareSwipe(final SwipeNative swipeNative, final AppiumDriver driver) {
        Dimension screenDimensions = driver.manage().window().getSize();
        int swipeValue = getSwipeValue(swipeNative, screenDimensions);
        Point start = SwipeType.PAGE == swipeNative.getType()
                ? UiUtil.getCenterPoint(driver)
                : UiUtil.findWebElement(dependencies, swipeNative.getLocator(), swipeNative.getLocatorStrategy())
                .getLocation();
        Point end = getEndPoint(swipeNative.getDirection(), start, swipeValue);
        return UiUtil.buildSequence(start, end, ACTION_DURATION);
    }

    private int getSwipeValue(final SwipeNative swipeNative, final Dimension screenDimensions) {
        switch (swipeNative.getDirection()) {
            case UP:
            case DOWN:
                return screenDimensions.height * swipeNative.getPercent() / PERCENTS;
            case LEFT:
            case RIGHT:
                return screenDimensions.width * swipeNative.getPercent() / PERCENTS;
            default:
                throw new DefaultFrameworkException(SWIPE_TYPE_NOT_FOUND, swipeNative.getDirection());
        }
    }

    private Point getEndPoint(final SwipeDirection direction, final Point start, final int swipeValue) {
        switch (direction) {
            case UP:
                return new Point(start.getX(), start.getY() - swipeValue);
            case DOWN:
                return new Point(start.getX(), start.getY() + swipeValue);
            case LEFT:
                return new Point(start.getX() + swipeValue, start.getY());
            case RIGHT:
                return new Point(start.getX() - swipeValue, start.getY());
            default:
                throw new DefaultFrameworkException(SWIPE_TYPE_NOT_FOUND, direction);
        }
    }
}
