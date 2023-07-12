package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.BrowserTab;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;

import java.util.LinkedList;
import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.TAB_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.TAB_OUT_OF_BOUNDS;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CLOSE_COMMAND;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CLOSE_TAB;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.SWITCH_COMMAND;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.SWITCH_TAB;
import static java.util.Objects.nonNull;

@Slf4j
@ExecutorForClass(BrowserTab.class)
public class BrowserTabExecutor extends AbstractUiExecutor<BrowserTab> {

    private final WebDriver driver;
    private final LinkedList<String> openedTabs;

    public BrowserTabExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = dependencies.getDriver();
        openedTabs = new LinkedList<>(driver.getWindowHandles());
    }

    @Override
    public void execute(final BrowserTab browserTab, final CommandResult result) {
        if (nonNull(browserTab.getClose())) {
            closeTab(browserTab.getClose().getIndex(), result);
        } else if (nonNull(browserTab.getOpen())) {
            openTab(browserTab.getOpen().getUrl(), result);
        } else if (nonNull(browserTab.getSwitch())) {
            switchToTab(browserTab.getSwitch().getIndex(), result);
        }
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void closeTab(final Integer tabIndex, final CommandResult result) {
        validateTabNumberOrThrow(tabIndex);
        if (Objects.isNull(tabIndex)) {
            driver.switchTo().window(openedTabs.pollLast()).close();
        } else {
            driver.switchTo().window(openedTabs.get(tabIndex - 1)).close();
            openedTabs.remove(tabIndex - 1);
        }
        ResultUtil.addCloseOrSwitchTabMetadata(CLOSE_COMMAND, tabIndex, result);
        LogUtil.logCloseOrSwitchTabCommand(CLOSE_TAB, tabIndex);
        driver.switchTo().window(openedTabs.peekLast());
    }

    private void switchToTab(final Integer tabIndex, final CommandResult result) {
        validateTabNumberOrThrow(tabIndex);
        if (Objects.isNull(tabIndex)) {
            driver.switchTo().window(openedTabs.pollLast());
        } else {
            driver.switchTo().window(openedTabs.get(tabIndex - 1));
        }
        ResultUtil.addCloseOrSwitchTabMetadata(SWITCH_COMMAND, tabIndex, result);
        LogUtil.logCloseOrSwitchTabCommand(SWITCH_TAB, tabIndex);
    }

    private void openTab(final String url, final CommandResult result) {
        if (dependencies.getUiType() == UiType.MOBILE_BROWSER) {
            ((JavascriptExecutor) driver).executeScript("window.open()");
            LinkedList<String> currentTabs = new LinkedList<>(driver.getWindowHandles());
            driver.switchTo().window(currentTabs.pollLast());
        } else {
            driver.switchTo().newWindow(WindowType.TAB);
        }
        if (StringUtils.isNotBlank(url)) {
            driver.navigate().to(UiUtil.getUrl(url, dependencies.getEnvironment(), dependencies.getUiType()));
        }
        ResultUtil.addOpenTabMetadata(url, result);
        LogUtil.logOpenTabCommand(url);
    }

    private void validateTabNumberOrThrow(final Integer tabIndex) {
        if (openedTabs.size() < 2) {
            throw new DefaultFrameworkException(TAB_NOT_FOUND);
        }
        if (nonNull(tabIndex) && tabIndex > openedTabs.size()) {
            throw new DefaultFrameworkException(TAB_OUT_OF_BOUNDS, tabIndex, openedTabs.size());
        }
    }
}
