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
import com.knubisoft.testlum.testing.model.scenario.SwipeElement;
import com.knubisoft.testlum.testing.model.scenario.SwipeNative;
import com.knubisoft.testlum.testing.model.scenario.SwipePage;
import io.appium.java_client.AppiumDriver;
import lombok.Getter;
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
        Swipe swipe = createSwipe(swipeNative, driver);

        for (int i = 0; i < swipe.getQuantity(); i++) {
            driver.perform(Collections.singletonList(swipe.getSequence()));
            driver.switchTo();
        }
    }

    private Swipe createSwipe(final SwipeNative swipeNative, final AppiumDriver driver) {
        if (swipeNative.getElement() != null) {
            return buildSwipe(swipeNative.getElement(), driver);
        }
        return buildSwipe(swipeNative.getPage(), driver);
    }

    private Swipe buildSwipe(final SwipeElement swipeElement, final AppiumDriver driver) {
        return buildSwipe(
                swipeElement.getPercent(),
                swipeElement.getDirection(),
                UiUtil.findWebElement(dependencies, swipeElement.getLocator(), swipeElement.getLocatorStrategy()).getLocation(),
                driver.manage().window().getSize(),
                swipeElement.getQuantity()
        );
    }

    private Swipe buildSwipe(final SwipePage swipePage, final AppiumDriver driver) {
        return buildSwipe(
                swipePage.getPercent(),
                swipePage.getDirection(),
                UiUtil.getCenterPoint(driver),
                driver.manage().window().getSize(),
                swipePage.getQuantity()
        );
    }

    private Swipe buildSwipe(final int percent, final SwipeDirection direction, final Point start, final Dimension screenSize, final int quantity) {
        int swipeValue = calculateSwipeValue(percent, direction, screenSize);
        Point end = calculateEndPoint(direction, start, swipeValue);
        return new Swipe(UiUtil.buildSequence(start, end, ACTION_DURATION), quantity);
    }

    private int calculateSwipeValue(final int percent, final SwipeDirection direction, final Dimension screenSize) {
        int size = (direction == SwipeDirection.UP || direction == SwipeDirection.DOWN) ? screenSize.height : screenSize.width;
        return size * percent / PERCENTS;
    }

    private Point calculateEndPoint(final SwipeDirection direction, final Point start, final int swipeValue) {
        switch (direction) {
            case UP:
                return new Point(start.getX(), start.getY() - swipeValue);
            case DOWN:
                return new Point(start.getX(), start.getY() + swipeValue);
            case LEFT:
                return new Point(start.getX() - swipeValue, start.getY());
            case RIGHT:
                return new Point(start.getX() + swipeValue, start.getY());
            default:
                throw new DefaultFrameworkException(SWIPE_TYPE_NOT_FOUND, direction);
        }
    }

    @Getter
    private static class Swipe {
        private final Sequence sequence;
        private final int quantity;

        public Swipe(final Sequence sequence, final int quantity) {
            this.sequence = sequence;
            this.quantity = quantity;
        }
    }

}
