package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;

import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.DragAndDropNative;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Sequence;

import java.util.Collections;

@ExecutorForClass(DragAndDropNative.class)
public class DragAndDropNativeExecutor extends AbstractUiExecutor<DragAndDropNative> {

    private static final int ACTION_DURATION = 1000;

    public DragAndDropNativeExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final DragAndDropNative dragAndDropNative, final CommandResult result) {
        LogUtil.logDragAndDropNativeInfo(dragAndDropNative);
        ResultUtil.addDragAndDropNativeMetaDada(dragAndDropNative, result);
        performDragAndDrop(dragAndDropNative);
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void performDragAndDrop(final DragAndDropNative dragAndDropNative) {
        AppiumDriver driver = (AppiumDriver) dependencies.getDriver();
        Point source = UiUtil.findWebElement(dependencies, dragAndDropNative.getFromLocator(),
                dragAndDropNative.getFromLocatorStrategy()).getLocation();
        Point target = UiUtil.findWebElement(dependencies, dragAndDropNative.getToLocator(),
                dragAndDropNative.getToLocatorStrategy()).getLocation();
        Sequence dragAndDrop = UiUtil.buildSequence(source, target, ACTION_DURATION);
        driver.perform(Collections.singletonList(dragAndDrop));
    }
}
