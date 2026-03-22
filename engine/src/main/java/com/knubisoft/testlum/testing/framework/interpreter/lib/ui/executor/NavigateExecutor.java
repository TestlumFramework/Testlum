package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Navigate;
import com.knubisoft.testlum.testing.model.scenario.NavigateCommand;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExecutorForClass(Navigate.class)
public class NavigateExecutor extends AbstractUiExecutor<Navigate> {

    public NavigateExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    //CHECKSTYLE:OFF
    @Override
    public void execute(final Navigate navigate, final CommandResult result) {
        NavigateCommand navigateCommand = navigate.getCommand();
        log.info(LogMessage.COMMAND_TYPE_LOG, navigateCommand.name());
        result.put(ResultUtil.NAVIGATE_TYPE, navigateCommand.value());
        switch (navigateCommand) {
            case BACK:
                dependencies.getDriver().navigate().back();
                break;
            case RELOAD:
                dependencies.getDriver().navigate().refresh();
                break;
            case TO:
                navigateTo(navigate.getPath(), result);
                break;
            default:
                throw new DefaultFrameworkException(ExceptionMessage.NAVIGATE_NOT_SUPPORTED, navigateCommand.value());
        }
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
    //CHECKSTYLE:ON

    private void navigateTo(final String path, final CommandResult result) {
        String url = uiUtil.getUrl(path, dependencies.getEnvironment(), dependencies.getUiType());
        dependencies.getDriver().navigate().to(url);
        result.put(ResultUtil.NAVIGATE_URL, path);
        log.info(LogMessage.BY_URL_LOG, path);
    }
}
