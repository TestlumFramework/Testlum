package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.SwipeElement;
import com.knubisoft.cott.testing.model.scenario.SwipeElementDirection;
import io.appium.java_client.AppiumDriver;
import java.util.Collections;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Sequence;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.PERFORM_ELEMENT_SWIPE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.SWIPE_VALUE;

@ExecutorForClass(SwipeElement.class)
public class SwipeElementExecutor extends AbstractUiExecutor<SwipeElement> {


    private static final int PERCENTS = 100;
    private static final int ACTION_DURATION = 250;

    public SwipeElementExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final SwipeElement swipeElement, final CommandResult result) {
        result.put(PERFORM_ELEMENT_SWIPE, swipeElement.getDirection());
        result.put(SWIPE_VALUE, swipeElement.getValueInPercents());
        LogUtil.logSwipeElementInfo(swipeElement);
        performElementSwipe(swipeElement);
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void performElementSwipe(final SwipeElement swipeElement) {
        AppiumDriver driver = (AppiumDriver) dependencies.getDriver();
        Dimension screenDimensions = driver.manage().window().getSize();
        int swipeValue = screenDimensions.width * swipeElement.getValueInPercents() / PERCENTS;
        Point start = UiUtil.findWebElement(driver, swipeElement.getLocatorId()).getLocation();
        Point end = SwipeElementDirection.LEFT.equals(swipeElement.getDirection())
                ? new Point(start.getX() + swipeValue, start.getY())
                : new Point(start.getX() - swipeValue, start.getY());
        Sequence swipe = UiUtil.buildSequence(start, end, ACTION_DURATION);
        driver.perform(Collections.singletonList(swipe));
    }
}
