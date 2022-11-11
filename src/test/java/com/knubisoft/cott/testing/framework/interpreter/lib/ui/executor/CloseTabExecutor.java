package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.CloseTab;
import java.util.LinkedList;
import org.openqa.selenium.WebDriver;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.TAB_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.TAB_OUT_OF_BOUNDS;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLOSE_COMMAND;

@ExecutorForClass(CloseTab.class)
public class CloseTabExecutor extends AbstractUiExecutor<CloseTab> {

    private final WebDriver driver;
    private final LinkedList<String> windows;

    public CloseTabExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = dependencies.getDriver();
        windows = new LinkedList<>(driver.getWindowHandles());
    }

    @Override
    public void execute(final CloseTab closeTab, final CommandResult result) {
        if (windows.size() < 2) {
            throw new DefaultFrameworkException(TAB_NOT_FOUND);
        }
        Integer tabNumber = closeTab.getTab();
        closeTab(result, tabNumber);
        driver.switchTo().window(windows.peekLast());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void closeTab(final CommandResult result, final Integer tabNumber) {
        if (tabNumber == null) {
            result.put(CLOSE_COMMAND, "last recently opened tab");
            driver.switchTo().window(windows.pollLast()).close();
        } else {
            validateTabNumberOrThrow(windows, tabNumber);
            result.put(CLOSE_COMMAND, tabNumber + " tab");
            driver.switchTo().window(windows.get(tabNumber - 1)).close();
            windows.remove(tabNumber - 1);
        }
    }

    private static void validateTabNumberOrThrow(final LinkedList<String> windows, final Integer tabNumber) {
        if (tabNumber > windows.size()) {
            throw new DefaultFrameworkException(TAB_OUT_OF_BOUNDS, tabNumber, windows.size());
        }
    }
}
