package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.DragAndDrop;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.io.File;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.DRAG_AND_DROP_FILE_NOT_FOUND;
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
        if (StringUtils.isNotBlank(dragAndDrop.getFileName())) {
            File source = FileSearcher.searchFileFromDir(dependencies.getFile(), dragAndDrop.getFileName());
            dropFile(target, source);
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
        if (!source.exists() || !source.isFile()) {
            throw new DefaultFrameworkException(DRAG_AND_DROP_FILE_NOT_FOUND, source.getName());
        }
        WebElement input = (WebElement) JavascriptUtil.executeJsScript(QUERY_FOR_DRAG_AND_DROP, driver, target);
        input.sendKeys(source.getPath());
    }
}
