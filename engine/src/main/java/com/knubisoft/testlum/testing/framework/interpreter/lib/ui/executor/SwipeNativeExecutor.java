package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Sequence;

import java.util.Collections;

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

        for (int i = 0; i < swipe.quantity(); i++) {
            driver.perform(Collections.singletonList(swipe.sequence()));
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
                UiUtil.findWebElement(dependencies, swipeElement.getLocator(),
                        swipeElement.getLocatorStrategy()).getLocation(),
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

    private Swipe buildSwipe(final int percent,
                             final SwipeDirection direction,
                             final Point start,
                             final Dimension screenSize,
                             final int quantity) {
        int swipeValue = calculateSwipeValue(percent, direction, screenSize);
        Point end = calculateEndPoint(direction, start, swipeValue);
        return new Swipe(UiUtil.buildSequence(start, end, ACTION_DURATION), quantity);
    }

    private int calculateSwipeValue(final int percent,
                                    final SwipeDirection direction,
                                    final Dimension screenSize) {
        int size = (direction == SwipeDirection.UP || direction == SwipeDirection.DOWN)
                ? screenSize.height : screenSize.width;
        return size * percent / PERCENTS;
    }

    private Point calculateEndPoint(final SwipeDirection direction, final Point start, final int swipeValue) {
        return switch (direction) {
            case UP -> new Point(start.getX(), start.getY() - swipeValue);
            case DOWN -> new Point(start.getX(), start.getY() + swipeValue);
            case LEFT -> new Point(start.getX() - swipeValue, start.getY());
            case RIGHT -> new Point(start.getX() + swipeValue, start.getY());
        };
    }

    private record Swipe(Sequence sequence, int quantity) {
    }

}
