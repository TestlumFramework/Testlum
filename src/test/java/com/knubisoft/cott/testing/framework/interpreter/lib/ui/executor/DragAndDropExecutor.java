package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.DragAndDrop;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.FILE_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.QUERY_FOR_DRAG_AND_DROP;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.FROM_LOCAL_FILE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.FROM_LOCATOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.TO_LOCATOR;

@ExecutorForClass(DragAndDrop.class)
public class DragAndDropExecutor extends AbstractUiExecutor<DragAndDrop> {

    private final WebDriver driver;

    public DragAndDropExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = dependencies.getDriver();
    }

    @Override
    public void execute(final DragAndDrop dragAndDrop, final CommandResult result) {
        WebElement target = UiUtil.findWebElement(driver, dragAndDrop.getToLocatorId());
        if (StringUtils.isNotBlank(dragAndDrop.getFilePath())) {
            dropFile(target, new File(dragAndDrop.getFilePath()));
            result.put(FROM_LOCAL_FILE, dragAndDrop.getFilePath());
        } else {
            dropElement(target, UiUtil.findWebElement(driver, dragAndDrop.getFromLocatorId()));
            result.put(FROM_LOCATOR, dragAndDrop.getFromLocatorId());
        }
        result.put(TO_LOCATOR, dragAndDrop.getToLocatorId());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void dropElement(final WebElement target, final WebElement source) {
        Actions action = new Actions(driver);
        action.dragAndDrop(source, target)
                .build()
                .perform();
    }

    public void dropFile(final WebElement target, final File source) {
        if (!source.exists()) {
            throw new DefaultFrameworkException(FILE_NOT_FOUND, source);
        }

        JavascriptExecutor jse = (JavascriptExecutor) driver;
        WebElement input = (WebElement) jse.executeScript(QUERY_FOR_DRAG_AND_DROP, target);
        input.sendKeys(source.getAbsolutePath());

    }
}
