package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.InnerScrollScript;
import com.knubisoft.testlum.testing.framework.util.PageScrollScript;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

import java.util.List;

@Slf4j
@ExecutorForClass(Scroll.class)
public class ScrollWebExecutor extends AbstractUiExecutor<Scroll> {

    private final InnerScrollScript innerScrollScript;

    public ScrollWebExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.innerScrollScript = dependencies.getContext().getBean(InnerScrollScript.class);
    }

    @Override
    public void execute(final Scroll scroll, final CommandResult result) {
        resultUtil.addScrollMetaData(scroll, result);
        uiLogUtil.logScrollInfo(scroll);
        executeScrollScript(scroll, dependencies.getDriver());
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void executeScrollScript(final Scroll scroll, final WebDriver webDriver) {
        switch (scroll.getType()) {
            case INNER:
                List<String> scrollScripts = innerScrollScript.getInnerScrollScript(scroll, uiUtil);
                tryToScroll(scrollScripts, webDriver);
                break;
            case PAGE:
                javascriptUtil.executeJsScript(PageScrollScript.getPageScrollScript(scroll, uiUtil), webDriver);
                break;
            default:
                throw new DefaultFrameworkException(ExceptionMessage.SCROLL_TYPE_NOT_FOUND, scroll.getType());
        }
    }

    private void tryToScroll(final List<String> scrollScripts, final WebDriver driver) {
        boolean anyLocatorSucceeded = false;
        for (String script : scrollScripts) {
            try {
                javascriptUtil.executeJsScript(script, driver);
                anyLocatorSucceeded = true;
            } catch (DefaultFrameworkException e) {
                log.debug("Scroll script failed for locator, trying next: {}", e.getMessage());
            }
        }
        if (!anyLocatorSucceeded) {
            throw new DefaultFrameworkException("Can't perform inner scroll");
        }
    }

}
