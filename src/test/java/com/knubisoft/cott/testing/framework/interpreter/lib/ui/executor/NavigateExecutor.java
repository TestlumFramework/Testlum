package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.Navigate;
import com.knubisoft.cott.testing.model.scenario.NavigateCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NAVIGATE_NOT_SUPPORTED;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.BY_URL_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.COMMAND_TYPE_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NAVIGATE_TYPE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NAVIGATE_URL;
import static java.lang.String.format;

@Slf4j
@ExecutorForClass(Navigate.class)
public class NavigateExecutor extends AbstractUiExecutor<Navigate> {

    private static final Pattern HTTP_PATTERN = Pattern.compile("https?://.+");

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
            default: throw new DefaultFrameworkException(format(NAVIGATE_NOT_SUPPORTED, navigateCommand.value()));
        }
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void navigateTo(final String path, final CommandResult result) {
        String url = inject(getUrl(path));
        result.put(NAVIGATE_URL, url);
        log.info(BY_URL_LOG, url);
        dependencies.getDriver().navigate().to(url);
    }

    private String getUrl(final String path) {
        if (HTTP_PATTERN.matcher(path).matches()) {
            return path;
        }
        if (UiType.MOBILE_BROWSER == dependencies.getUiType()) {
            return GlobalTestConfigurationProvider.getMobilebrowserSettings(dependencies.getEnvironment())
                    .getBaseUrl() + path;
        }
        return GlobalTestConfigurationProvider.getWebSettings(dependencies.getEnvironment()).getBaseUrl() + path;
    }
}
