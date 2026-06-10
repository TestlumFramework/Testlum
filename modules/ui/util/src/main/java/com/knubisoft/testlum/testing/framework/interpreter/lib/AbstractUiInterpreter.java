package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Ui;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public abstract class AbstractUiInterpreter<T extends Ui> extends AbstractInterpreter<T> {

    private static final String CLEAR_COOKIES_AFTER_EXECUTION = "Clear cookies after execution";
    private static final String CLEAR_LOCAL_STORAGE_BY_KEY = "Clear local storage by key";
    protected final SubCommandRunner subCommandRunner;

    protected AbstractUiInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        subCommandRunner = dependencies.getContext().getBean(SubCommandRunner.class);
    }

    public ExecutorDependencies createExecutorDependencies(final UiType uiType) {
        return ExecutorDependencies.builder()
                .context(dependencies.getContext())
                .file(dependencies.getFile())
                .driver(uiType.getAppropriateDriver(dependencies))
                .scenarioContext(dependencies.getScenarioContext())
                .position(dependencies.getPosition())
                .uiType(uiType)
                .environment(dependencies.getEnvironment())
                .build();
    }

    public void clearLocalStorage(final WebDriver driver, final String key, final CommandResult result) {
        if (StringUtils.isNotEmpty(key)) {
            result.put(CLEAR_LOCAL_STORAGE_BY_KEY, key);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.localStorage.removeItem(arguments[0]);", key);
        }
    }

    public void clearCookies(final WebDriver driver, final boolean clearCookies, final CommandResult result) {
        result.put(CLEAR_COOKIES_AFTER_EXECUTION, clearCookies);
        if (clearCookies) {
            driver.manage().deleteAllCookies();
        }
    }
}
