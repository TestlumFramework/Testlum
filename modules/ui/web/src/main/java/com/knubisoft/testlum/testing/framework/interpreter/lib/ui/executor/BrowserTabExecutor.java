package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.BrowserTab;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;

import java.util.LinkedList;
import java.util.Objects;

@Slf4j
@ExecutorForClass(BrowserTab.class)
public class BrowserTabExecutor extends AbstractUiExecutor<BrowserTab> {

    private static final String WINDOW_OPEN = "window.open()";

    private final WebDriver driver;
    private final LinkedList<String> openedTabs;

    public BrowserTabExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.driver = dependencies.getDriver();
        this.openedTabs = new LinkedList<>(driver.getWindowHandles());
    }

    @Override
    public void execute(final BrowserTab browserTab, final CommandResult result) {
        if (Objects.nonNull(browserTab.getClose())) {
            closeTab(browserTab.getClose().getIndex(), result);
        } else if (Objects.nonNull(browserTab.getOpen())) {
            openTab(browserTab.getOpen().getUrl(), result);
        } else if (Objects.nonNull(browserTab.getSwitch())) {
            switchToTab(browserTab.getSwitch().getIndex(), result);
        }
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void closeTab(final Integer tabIndex, final CommandResult result) {
        validateTabNumberOrThrow(tabIndex);
        if (Objects.isNull(tabIndex)) {
            driver.switchTo().window(openedTabs.pollLast()).close();
        } else {
            driver.switchTo().window(openedTabs.get(tabIndex - 1)).close();
            openedTabs.remove(tabIndex - 1);
        }
        resultUtil.addCloseOrSwitchTabMetadata(ResultUtil.CLOSE_COMMAND, tabIndex, result);
        uiLogUtil.logCloseOrSwitchTabCommand(ResultUtil.CLOSE_TAB, tabIndex);
        driver.switchTo().window(openedTabs.peekLast());
    }

    private void switchToTab(final Integer tabIndex, final CommandResult result) {
        validateTabNumberOrThrow(tabIndex);
        if (Objects.isNull(tabIndex)) {
            driver.switchTo().window(openedTabs.pollLast());
        } else {
            driver.switchTo().window(openedTabs.get(tabIndex - 1));
        }
        resultUtil.addCloseOrSwitchTabMetadata(ResultUtil.SWITCH_COMMAND, tabIndex, result);
        uiLogUtil.logCloseOrSwitchTabCommand(ResultUtil.SWITCH_TAB, tabIndex);
    }

    private void openTab(final String url, final CommandResult result) {
        if (dependencies.getUiType() == UiType.MOBILE_BROWSER) {
            javascriptUtil.executeJsScript(WINDOW_OPEN, driver);
            LinkedList<String> currentTabs = new LinkedList<>(driver.getWindowHandles());
            driver.switchTo().window(currentTabs.pollLast());
        } else {
            driver.switchTo().newWindow(WindowType.TAB);
        }
        if (StringUtils.isNotBlank(url)) {
            driver.navigate().to(uiUtil.getUrl(url, dependencies.getEnvironment(), dependencies.getUiType()));
        }
        resultUtil.addOpenTabMetadata(url, result);
        uiLogUtil.logOpenTabCommand(url);
    }

    private void validateTabNumberOrThrow(final Integer tabIndex) {
        if (openedTabs.size() < 2) {
            throw new DefaultFrameworkException(ExceptionMessage.TAB_NOT_FOUND);
        }
        if (Objects.nonNull(tabIndex) && tabIndex > openedTabs.size()) {
            throw new DefaultFrameworkException(ExceptionMessage.TAB_OUT_OF_BOUNDS, tabIndex, openedTabs.size());
        }
    }
}
