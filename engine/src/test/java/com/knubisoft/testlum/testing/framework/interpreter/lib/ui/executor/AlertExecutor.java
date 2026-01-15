package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Alert;
import com.knubisoft.testlum.testing.model.scenario.AlertAction;
import com.knubisoft.testlum.testing.model.scenario.AlertType;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ALERT_NOT_VISIBLE;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ALERT_TYPE;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ALERT_WAIT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.PROMPT_TEXT;

@ExecutorForClass(Alert.class)
public class AlertExecutor extends AbstractUiExecutor<Alert> {

    public AlertExecutor(ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(Alert alert, CommandResult result) {
        result.put(ALERT_TYPE, alert.getType().value());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        waitForAlertVisibleIfRequired(alert, result);
        handleAlertInteraction(alert, result);
    }

    private void waitForAlertVisibleIfRequired(Alert alert, CommandResult result) {
        if (alert.isWaitUntilVisible() != null && Boolean.TRUE.equals(alert.isWaitUntilVisible())) {
            result.put(ALERT_WAIT, alert.getTimeout());
            WebDriverWait wait = new WebDriverWait(dependencies.getDriver(), Duration.ofSeconds(alert.getTimeout()));
            try {
                wait.until(ExpectedConditions.alertIsPresent());
            } catch (TimeoutException e) {
                throw new DefaultFrameworkException(ALERT_NOT_VISIBLE);
            }
        }
    }

    private void handleAlertInteraction(Alert alert, CommandResult result) {
        fillPromptTextIfRequired(alert, result);
        finalizeAlert(alert);
    }

    private void fillPromptTextIfRequired(Alert alert, CommandResult result) {
        if (AlertType.PROMPT == alert.getType() && alert.getText() != null) {
            String promptText = alert.getText();
            result.put(PROMPT_TEXT, promptText);
            setPromptText(promptText);
        }
    }

    private void finalizeAlert(Alert alert) {
        if (AlertAction.ACCEPT == alert.getAction()) {
            acceptAlert();
        } else {
            dismissAlert();
        }
    }

    private void acceptAlert() {
        dependencies.getDriver().switchTo().alert().accept();
    }

    private void dismissAlert() {
        dependencies.getDriver().switchTo().alert().dismiss();
    }

    private void setPromptText(String alertText) {
        dependencies.getDriver().switchTo().alert().sendKeys(alertText);
    }
}
