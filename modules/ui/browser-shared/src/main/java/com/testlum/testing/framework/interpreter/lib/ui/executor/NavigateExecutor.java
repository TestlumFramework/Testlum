package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.model.scenario.Navigate;
import com.testlum.testing.model.scenario.NavigateCommand;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExecutorForClass(Navigate.class)
public class NavigateExecutor extends AbstractUiExecutor<Navigate> {

    public NavigateExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Navigate navigate, final CommandResult result) {
        NavigateCommand navigateCommand = navigate.getCommand();
        log.info(LogMessage.COMMAND_TYPE_LOG, navigateCommand.name());
        result.put(ResultUtil.NAVIGATE_TYPE, navigateCommand.value());
        switch (navigateCommand) {
            case BACK -> dependencies.getDriver().navigate().back();
            case RELOAD -> dependencies.getDriver().navigate().refresh();
            case TO -> navigateTo(navigate.getPath(), result);
            default -> throw new DefaultFrameworkException(
                    ExceptionMessage.NAVIGATE_NOT_SUPPORTED, navigateCommand.value());
        }
        screenshotUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void navigateTo(final String path, final CommandResult result) {
        String url = uiUtil.getUrl(path, dependencies.getEnvironment(), dependencies.getUiType());
        dependencies.getDriver().navigate().to(url);
        result.put(ResultUtil.NAVIGATE_URL, path);
        log.info(LogMessage.BY_URL_LOG, path);
    }
}
