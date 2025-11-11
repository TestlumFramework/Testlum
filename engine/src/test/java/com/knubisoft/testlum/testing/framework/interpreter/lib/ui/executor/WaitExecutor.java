package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.framework.wait.util.WaitUtil;
import com.knubisoft.testlum.testing.model.global_config.Web;
import com.knubisoft.testlum.testing.model.scenario.CommandWithLocator;
import com.knubisoft.testlum.testing.model.scenario.UiWait;
import com.knubisoft.testlum.testing.model.scenario.Visible;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.LOCATOR_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WAIT_INFO_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WAIT_TYPE;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.LOCATOR_ID;
import static java.util.Objects.nonNull;

@ExecutorForClass(UiWait.class)
@Slf4j
public class WaitExecutor extends AbstractUiExecutor<UiWait> {

    private final WaitUtil waitUtil;

    public WaitExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.waitUtil = dependencies.getContext().getBean(WaitUtil.class);
    }

    @Override
    public void execute(final UiWait uiWait, final CommandResult result) {
        String time = uiWait.getTime();
        log.info(WAIT_INFO_LOG, time, uiWait.getUnit());
        TimeUnit timeUnit = waitUtil.getTimeUnit(uiWait.getUnit());
        ResultUtil.addWaitMetaData(time, timeUnit, result);
        wait(uiWait, time, timeUnit, result);
    }

    private void wait(final UiWait wait, final String time, final TimeUnit timeUnit, final CommandResult result) {
        try {
            if (nonNull(wait.getVisible())) {
                waitIfVisibleOrClickable(wait.getVisible(), timeUnit.toSeconds(Long.parseLong(time)), result);
            } else if (nonNull(wait.getClickable())) {
                waitIfVisibleOrClickable(wait.getClickable(), timeUnit.toSeconds(Long.parseLong(time)), result);
            } else {
                waitUtil.sleep(Long.parseLong(time), timeUnit);
            }
        } catch (Exception e) {
            LogUtil.logException(e);
            ResultUtil.setExceptionResult(result, e);
            ConfigUtil.checkIfStopScenarioOnFailure(e);
        }
    }

    private void waitIfVisibleOrClickable(final CommandWithLocator command,
                                          final Long seconds,
                                          final CommandResult result) {
        log.info(LOCATOR_LOG, command.getLocator());
        result.put(LOCATOR_ID, command.getLocator());

        final Duration implicitFromConfig = getImplicitFromConfigSafe();
        WebDriver driver = dependencies.getDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);

        try {
            FindOptions.State state = (command instanceof Visible)
                    ? FindOptions.State.VISIBLE
                    : FindOptions.State.CLICKABLE;

            UiUtil.findWebElement(
                    dependencies,
                    command.getLocator(),
                    command.getLocatorStrategy(),
                    FindOptions.builder()
                            .maxTime(Duration.ofSeconds(seconds))
                            .pollInterval(Duration.ofMillis(500))
                            .useConfigSleep(false)
                            .waitForDomComplete(false)
                            .state(state)
                            .build()
            );

            log.info(WAIT_TYPE, state == FindOptions.State.VISIBLE ? "Visible" : "Clickable");

        } finally {
            driver.manage().timeouts().implicitlyWait(implicitFromConfig);
        }
    }

    private Duration getImplicitFromConfigSafe() {
        try {
            Web settings = ConfigProviderImpl.GlobalTestConfigurationProvider.getWebSettings(EnvManager.currentEnv());
            int secondsToWait = settings.getBrowserSettings().getElementAutowait().getSeconds();
            return Duration.ofSeconds(secondsToWait);
        } catch (Exception ignore) {
            return Duration.ZERO;
        }
    }
}
