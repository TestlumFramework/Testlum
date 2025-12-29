package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Alert;
import com.knubisoft.testlum.testing.model.scenario.AlertAction;
import com.knubisoft.testlum.testing.model.scenario.AlertType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ALERT_TYPE;

@ExecutorForClass(Alert.class)
public class AlertExecutor extends AbstractUiExecutor<Alert> {

    public AlertExecutor(ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(Alert alert, CommandResult result) {
        result.put(ALERT_TYPE, alert.getType().value());
//        UiUtil.waitForElementVisibility(dependencies, webElement);
//        UiUtil.highlightElementIfRequired(click.isHighlight(), webElement, dependencies.getDriver());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        processAlertCommand(alert, result);
//        clickWithMethod(click.getMethod(), webElement, result);
    }

    private void processAlertCommand(Alert alert, CommandResult result) {
        if (AlertType.PROMPT == alert.getType() && alert.getText() != null) {
//            result.put(CLICK_METHOD, "javascript");
           setPromptText(alert.getText());
        }

        WebDriverWait wait = new WebDriverWait(dependencies.getDriver(), Duration.ofSeconds(5));
        wait.until(ExpectedConditions.alertIsPresent());

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

    private String getAlertText(Object alert) {
        return dependencies.getDriver().switchTo().alert().getText();
    }

    private void setPromptText(String alertText) {
        dependencies.getDriver().switchTo().alert().sendKeys(alertText);
    }

    /*

    <alert type="alert" action="dismiss" />
    <alert type="prompt" text="Hello" action="accept" />

assert alert is present
var from alert
     */
}
