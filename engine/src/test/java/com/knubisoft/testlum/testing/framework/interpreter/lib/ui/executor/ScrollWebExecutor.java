package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.InnerScrollScript;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.PageScrollScript;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;

import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import org.openqa.selenium.WebDriver;

import java.util.List;

@ExecutorForClass(Scroll.class)
public class ScrollWebExecutor extends AbstractUiExecutor<Scroll> {

    public ScrollWebExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Scroll scroll, final CommandResult result) {
        ResultUtil.addScrollMetaData(scroll, result);
        LogUtil.logScrollInfo(scroll);
        executeScrollScript(scroll, dependencies.getDriver());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void executeScrollScript(final Scroll scroll, final WebDriver webDriver) {
        switch (scroll.getType()) {
            case INNER:
                List<String> scrollScripts = InnerScrollScript.getInnerScrollScript(scroll);
                tryToScroll(scrollScripts, webDriver);
                break;
            case PAGE:
                JavascriptUtil.executeJsScript(PageScrollScript.getPageScrollScript(scroll), webDriver);
                break;
            default:
                throw new DefaultFrameworkException(ExceptionMessage.SCROLL_TYPE_NOT_FOUND, scroll.getType());
        }
    }

    private void tryToScroll(final List<String> scrollScripts, final WebDriver driver) {
        boolean anyLocatorSucceeded = false;
        for (String script : scrollScripts) {
            try {
                JavascriptUtil.executeJsScript(script, driver);
                anyLocatorSucceeded = true;
            } catch (DefaultFrameworkException ignored) {
                //ignored
            }
        }
        if (!anyLocatorSucceeded) {
            throw new DefaultFrameworkException("Can't perform inner scroll");
        }
    }

}
