package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.NavigateUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Navigate;
import com.knubisoft.testlum.testing.model.scenario.NavigateCommand;
import lombok.extern.slf4j.Slf4j;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NAVIGATE_NOT_SUPPORTED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.BY_URL_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMAND_TYPE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.NAVIGATE_TYPE;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.NAVIGATE_URL;

@Slf4j
@ExecutorForClass(Navigate.class)
public class NavigateExecutor extends AbstractUiExecutor<Navigate> {

    public NavigateExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Navigate navigate, final CommandResult result) {
        NavigateCommand navigateCommand = navigate.getCommand();
        log.info(COMMAND_TYPE_LOG, navigateCommand.name());
        result.put(NAVIGATE_TYPE, navigateCommand.value());
        switch (navigateCommand) {
            case BACK: dependencies.getDriver().navigate().back();
                break;
            case RELOAD: dependencies.getDriver().navigate().refresh();
                break;
            case TO: navigateTo(navigate.getPath(), result);
                break;
            default: throw new DefaultFrameworkException(NAVIGATE_NOT_SUPPORTED, navigateCommand.value());
        }
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void navigateTo(final String path, final CommandResult result) {
        NavigateUtil.navigateTo(path, dependencies);
        result.put(NAVIGATE_URL, path);
        log.info(BY_URL_LOG, path);
    }
}
