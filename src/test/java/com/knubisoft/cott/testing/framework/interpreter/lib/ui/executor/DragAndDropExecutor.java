package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.DragAndDrop;
import java.io.File;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.FILE_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.QUERY_FOR_DRAG_AND_DROP;

@ExecutorForClass(DragAndDrop.class)
public class DragAndDropExecutor extends AbstractUiExecutor<DragAndDrop> {

    private final WebDriver driver;

    protected DragAndDropExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = dependencies.getDriver();
    }

    @Override
    public void execute(final DragAndDrop dragAndDrop, final CommandResult result) {
        WebElement target = UiUtil.findWebElement(driver, dragAndDrop.getToLocatorId());
        if (dragAndDrop.isDropFile()) {
            dropFile(new File(dragAndDrop.getFilePath()), target);
        } else {
            Actions action = new Actions(driver);
            action.dragAndDrop(UiUtil.findWebElement(driver, dragAndDrop.getFromLocatorId()), target)
                    .build()
                    .perform();
        }
    }
    public void dropFile(final File filePath, final WebElement target) {
        if (!filePath.exists()) {
            throw new DefaultFrameworkException(FILE_NOT_FOUND, filePath);
        }

        JavascriptExecutor jse = (JavascriptExecutor) driver;
        WebElement input = (WebElement) jse.executeScript(QUERY_FOR_DRAG_AND_DROP, target, 0, 0);
        input.sendKeys(filePath.getAbsoluteFile().toString());
    }
}
