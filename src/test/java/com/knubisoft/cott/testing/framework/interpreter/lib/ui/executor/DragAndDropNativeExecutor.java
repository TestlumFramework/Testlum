package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.DragAndDropNative;
import io.appium.java_client.AppiumDriver;
import java.util.Collections;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Sequence;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.FROM_LOCATOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.TO_LOCATOR;

@ExecutorForClass(DragAndDropNative.class)
public class DragAndDropNativeExecutor extends AbstractUiExecutor<DragAndDropNative> {

    private static final int ACTION_DURATION = 1000;

    public DragAndDropNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final DragAndDropNative dragAndDropNative, final CommandResult result) {
        result.put(FROM_LOCATOR, dragAndDropNative.getFromLocatorId());
        result.put(TO_LOCATOR, dragAndDropNative.getToLocatorId());
        LogUtil.logDragAndDropNativeInfo(dragAndDropNative);
        performDragAndDrop(dragAndDropNative);
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void performDragAndDrop(final DragAndDropNative dragAndDropNative) {
        AppiumDriver driver = (AppiumDriver) dependencies.getDriver();
        Point source = UiUtil.findWebElement(driver, dragAndDropNative.getFromLocatorId()).getLocation();
        Point target = UiUtil.findWebElement(driver, dragAndDropNative.getToLocatorId()).getLocation();
        Sequence dragAndDrop = UiUtil.buildSequence(source, target, ACTION_DURATION);
        driver.perform(Collections.singletonList(dragAndDrop));
    }

}
