package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.CloseSecondTab;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SECOND_TAB_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLOSE_COMMAND;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.SECOND_TAB;

@ExecutorForClass(CloseSecondTab.class)
public class CloseSecondTabExecutor extends AbstractUiExecutor<CloseSecondTab> {

    public CloseSecondTabExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final CloseSecondTab closeSecondTab, final CommandResult result) {
        WebDriver driver = dependencies.getDriver();
        result.put(CLOSE_COMMAND, SECOND_TAB);
        List<String> windowHandles = new ArrayList<>(driver.getWindowHandles());
        if (windowHandles.size() <= 1) {
            throw new DefaultFrameworkException(SECOND_TAB_NOT_FOUND);
        }
        driver.switchTo().window(windowHandles.get(1));
        driver.close();
        driver.switchTo().window(windowHandles.get(0));
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
