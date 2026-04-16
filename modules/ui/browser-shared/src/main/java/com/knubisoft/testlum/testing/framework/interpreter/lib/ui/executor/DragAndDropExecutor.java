package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.constant.JavascriptConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.DragAndDrop;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.File;

@ExecutorForClass(DragAndDrop.class)
public class DragAndDropExecutor extends AbstractUiExecutor<DragAndDrop> {

    private final WebDriver driver;

    public DragAndDropExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.driver = dependencies.getDriver();
    }

    public void execute(final DragAndDrop dragAndDrop, final CommandResult result) {
        logUtil.logDragAndDropInfo(dragAndDrop);
        resultUtil.addDragAndDropMetaDada(dragAndDrop, result);
        WebElement target = uiUtil.findWebElement(dependencies, dragAndDrop.getToLocator(),
                dragAndDrop.getToLocatorStrategy());
        if (StringUtils.isNotBlank(dragAndDrop.getFileName())) {
            File source = fileSearcher.searchFileFromDir(
                    dependencies.getFile().getParentFile(), dragAndDrop.getFileName());
            dropFile(target, source);
        } else {
            dropElement(target, uiUtil.findWebElement(dependencies, dragAndDrop.getFromLocator(),
                    dragAndDrop.getToLocatorStrategy()));
        }
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void dropElement(final WebElement target, final WebElement source) {
        Actions action = new Actions(driver);
        action.dragAndDrop(source, target)
                .build()
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
