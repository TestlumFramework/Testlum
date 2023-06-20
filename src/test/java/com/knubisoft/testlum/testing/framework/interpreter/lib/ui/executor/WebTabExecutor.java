package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.NavigateUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.WebTab;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.TAB_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.TAB_OUT_OF_BOUNDS;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CLOSE_COMMAND;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CLOSE_TAB;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.LAST_TAB;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.NEW_TAB;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.NEW_TAB_WITH_URL;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.OPEN_COMMAND;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.OPEN_TAB;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.SWITCH_COMMAND;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.SWITCH_TAB;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.TAB_NUMBER;
import static java.util.Objects.nonNull;

@Slf4j
@ExecutorForClass(WebTab.class)
public class WebTabExecutor extends AbstractUiExecutor<WebTab> {

    private final WebDriver driver;
    private final LinkedList<String> windows;

    public WebTabExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = dependencies.getDriver();
        windows = new LinkedList<>(driver.getWindowHandles());
    }

    @Override
    public void execute(final WebTab webTab, final CommandResult result) {
        if (nonNull(webTab.getClose())) {
            closeTab(webTab.getClose().getTabIndex(), result);
        } else if (nonNull(webTab.getOpen())) {
            openNewTab(webTab.getOpen().getUrl(), result);
        } else if (nonNull(webTab.getSwitch())) {
            switchToTab(webTab.getSwitch().getTabIndex(), result);
        }
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void closeTab(final Integer tabIndex, final CommandResult result) {
        validateTabNumberOrThrow(tabIndex);
        if (Objects.isNull(tabIndex)) {
            driver.switchTo().window(windows.pollLast()).close();
        } else {
            driver.switchTo().window(windows.get(tabIndex - 1)).close();
            windows.remove(tabIndex - 1);
        }
        result.put(CLOSE_COMMAND, String.format(TAB_NUMBER, nonNull(tabIndex) ? tabIndex : LAST_TAB));
        LogUtil.logWebTabCommand(CLOSE_TAB, tabIndex, null);
        driver.switchTo().window(windows.peekLast());
    }

    private void switchToTab(final Integer tabIndex, final CommandResult result) {
        validateTabNumberOrThrow(tabIndex);
        if (Objects.isNull(tabIndex)) {
            driver.switchTo().window(windows.pollLast());
        } else {
            driver.switchTo().window(windows.get(tabIndex));
        }
        result.put(SWITCH_COMMAND, String.format(TAB_NUMBER, nonNull(tabIndex) ? tabIndex : LAST_TAB));
        LogUtil.logWebTabCommand(SWITCH_TAB, tabIndex, null);
    }

    private void openNewTab(final String url, final CommandResult result) {
        if (!StringUtils.hasText(url)) {
            driver.switchTo().newWindow(WindowType.TAB);
        } else {
            driver.switchTo().newWindow(WindowType.TAB);
            NavigateUtil.navigateTo(url, dependencies);
        }
        result.put(OPEN_COMMAND, String.format(NEW_TAB_WITH_URL, StringUtils.hasText(url) ? url : NEW_TAB));
        LogUtil.logWebTabCommand(OPEN_TAB, null, url);
    }

    private void validateTabNumberOrThrow(final Integer tabNumber) {
        if (windows.size() < 2) {
            throw new DefaultFrameworkException(TAB_NOT_FOUND);
        }
        if (nonNull(tabNumber) && tabNumber > windows.size()) {
            throw new DefaultFrameworkException(TAB_OUT_OF_BOUNDS, tabNumber, windows.size());
        }
    }
}
