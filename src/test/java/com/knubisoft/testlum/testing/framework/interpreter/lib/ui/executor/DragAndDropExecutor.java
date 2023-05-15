package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.DragAndDrop;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.io.File;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FILE_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.JavascriptConstant.QUERY_FOR_DRAG_AND_DROP;

@ExecutorForClass(DragAndDrop.class)
public class DragAndDropExecutor extends AbstractUiExecutor<DragAndDrop> {

    private final WebDriver driver;

    public DragAndDropExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = dependencies.getDriver();
    }

    @Override
    public void execute(final DragAndDrop dragAndDrop, final CommandResult result) {
        LogUtil.logDragAndDropInfo(dragAndDrop);
        ResultUtil.addDragAndDropMetaDada(dragAndDrop, result);
        WebElement target = UiUtil.findWebElement(dependencies, dragAndDrop.getToLocatorId());
        if (StringUtils.isNotBlank(dragAndDrop.getFilePath())) {
            dropFile(target, new File(dragAndDrop.getFilePath()));
        } else {
            dropElement(target, UiUtil.findWebElement(dependencies, dragAndDrop.getFromLocatorId()));
        }
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void dropElement(final WebElement target, final WebElement source) {
        Actions action = new Actions(driver);
        action.dragAndDrop(source, target)
                .build()
                .perform();
    }

    public void dropFile(final WebElement target, final File source) {
        File file = getDropFile(source);
        if (!file.exists()) {
            throw new DefaultFrameworkException(FILE_NOT_FOUND, file);
        }
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        WebElement input = (WebElement) javascriptExecutor.executeScript(QUERY_FOR_DRAG_AND_DROP, target);
        input.sendKeys(file.getAbsolutePath());
    }

    private File getDropFile(final File source) {
        String fileName = source.getName();
        File scenarioFolder = dependencies.getFile().getParentFile();
        return new File(scenarioFolder, fileName);
    }
}
