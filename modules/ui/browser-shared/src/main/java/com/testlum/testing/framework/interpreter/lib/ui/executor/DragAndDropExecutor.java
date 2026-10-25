package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.constant.JavascriptConstant;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.scenario.DragAndDrop;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.File;
import java.time.Duration;

@ExecutorForClass(DragAndDrop.class)
public class DragAndDropExecutor extends AbstractUiExecutor<DragAndDrop> {

    private static final int DRAG_AND_DROP_OFFSET = 1;
    private static final int DRAG_AND_DROP_TIMEOUT_MS = 300;

    private final WebDriver driver;

    public DragAndDropExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.driver = dependencies.getDriver();
    }

    public void execute(final DragAndDrop dragAndDrop, final CommandResult result) {
        uiLogUtil.logDragAndDropInfo(dragAndDrop);
        resultUtil.addDragAndDropMetaDada(dragAndDrop, result);
        WebElement target = uiUtil.findWebElement(dependencies, dragAndDrop.getToLocator(),
                dragAndDrop.getToLocatorStrategy(), ElementChecks.FOR_POSITIONING, result);
        if (StringUtils.isNotBlank(dragAndDrop.getFileName())) {
            File source = fileSearcher.searchFileFromDir(
                    dependencies.getFile().getParentFile(), dragAndDrop.getFileName());
            dropFile(target, source);
        } else {
            dropElement(target, uiUtil.findWebElement(dependencies, dragAndDrop.getFromLocator(),
                    dragAndDrop.getToLocatorStrategy(), ElementChecks.FOR_POSITIONING, result));
        }
        screenshotUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void dropElement(final WebElement target, final WebElement source) {
        Actions action = new Actions(driver);
        action.clickAndHold(source)
                .moveByOffset(DRAG_AND_DROP_OFFSET, DRAG_AND_DROP_OFFSET)
                .pause(Duration.ofMillis(DRAG_AND_DROP_TIMEOUT_MS))
                .moveToElement(target)
                .release(target)
                .perform();
    }

    public void dropFile(final WebElement target, final File source) {
        if (!source.exists() || !source.isFile()) {
            throw new DefaultFrameworkException(ExceptionMessage.DRAG_AND_DROP_FILE_NOT_FOUND, source.getName());
        }
        WebElement input = resolveInputElement(target);
        sendFileToInput(input, source.getAbsolutePath());
    }

    private WebElement resolveInputElement(final WebElement target) {
        if (target.getTagName().equalsIgnoreCase("input")) {
            return target;
        }
        return (WebElement) javascriptUtil.executeJsScript(
                JavascriptConstant.QUERY_FOR_DRAG_AND_DROP, driver, target);
    }

    private void sendFileToInput(final WebElement input, final String filePath) {
        try {
            input.sendKeys(filePath);
        } catch (InvalidArgumentException e) {
            ((RemoteWebElement) input).setFileDetector(new LocalFileDetector());
            input.sendKeys(filePath);
        }
    }
}
