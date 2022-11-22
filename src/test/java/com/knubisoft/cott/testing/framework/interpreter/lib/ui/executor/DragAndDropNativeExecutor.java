package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.DragAndDropNative;
import io.appium.java_client.AppiumDriver;
import java.time.Duration;
import java.util.Collections;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

@ExecutorForClass(DragAndDropNative.class)
public class DragAndDropNativeExecutor extends AbstractUiExecutor<DragAndDropNative> {
    private final AppiumDriver driver;

    public DragAndDropNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = (AppiumDriver) dependencies.getDriver();
    }

    @Override
    public void execute(final DragAndDropNative dragAndDropNative, final CommandResult result) {
        WebElement from = UiUtil.findWebElement(driver, dragAndDropNative.getFromLocatorId());
        WebElement to = UiUtil.findWebElement(driver, dragAndDropNative.getToLocatorId());
        driver.perform(Collections.singletonList(setupDragAndDrop(from, to)));
    }

    private static Sequence setupDragAndDrop(final WebElement from, final WebElement to) {
        Point source = from.getLocation();
        Point target = to.getLocation();
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence dragNDrop = new Sequence(finger, 1);
        dragNDrop.addAction(finger.createPointerMove(Duration.ofMillis(0),
                PointerInput.Origin.viewport(), source.x, source.y));
        dragNDrop.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        dragNDrop.addAction(finger.createPointerMove(Duration.ofSeconds(1),
                PointerInput.Origin.viewport(), target.x, target.y));
        dragNDrop.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        return dragNDrop;
    }
}
