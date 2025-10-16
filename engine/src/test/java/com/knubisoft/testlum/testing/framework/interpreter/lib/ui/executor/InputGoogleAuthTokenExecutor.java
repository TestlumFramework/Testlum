package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.InputGoogleAuthToken;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.VALUE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.INPUT_LOCATOR;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.INPUT_VALUE;

@Slf4j
@ExecutorForClass(InputGoogleAuthToken.class)
public class InputGoogleAuthTokenExecutor extends AbstractUiExecutor<InputGoogleAuthToken> {

	public InputGoogleAuthTokenExecutor(ExecutorDependencies dependencies) {
		super(dependencies);
	}

	@Override
	public void execute(final InputGoogleAuthToken o, final CommandResult result) {
		result.put(INPUT_LOCATOR, o.getLocator());
		WebElement webElement = UiUtil.findWebElement(dependencies, o.getLocator(), o.getLocatorStrategy());
		UiUtil.waitForElementVisibility(dependencies, webElement);
		String value = o.getSecretKey();
		result.put(INPUT_VALUE, value);
		log.info(VALUE_LOG, value);
		webElement.sendKeys(value);
		UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
	}
}
