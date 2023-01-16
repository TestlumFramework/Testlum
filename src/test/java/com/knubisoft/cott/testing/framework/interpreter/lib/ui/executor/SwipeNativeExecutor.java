package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.SwipeDirection;
import com.knubisoft.cott.testing.model.scenario.SwipeNative;
import com.knubisoft.cott.testing.model.scenario.SwipeType;
import io.appium.java_client.AppiumDriver;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Sequence;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.CANNOT_SWIPE_ELEMENT;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SWIPE_TYPE_NOT_FOUND;

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
        int swipeValue = screenDimensions.width * swipeNative.getValueInPercents() / PERCENTS;
        Point start = SwipeType.PAGE.equals(swipeNative.getType())
                ? UiUtil.getCenterPoint(driver)
                : getElementLocation(swipeNative, driver);
        Point end = getEndPoint(swipeNative.getDirection(), start, swipeValue);
        return UiUtil.buildSequence(start, end, ACTION_DURATION);
    }

    private Point getElementLocation(final SwipeNative swipeNative, final AppiumDriver driver) {
        if (!StringUtils.isBlank(swipeNative.getLocator())) {
            return UiUtil.findWebElement(driver, swipeNative.getLocator()).getLocation();
        } else {
            throw new DefaultFrameworkException(CANNOT_SWIPE_ELEMENT);
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
