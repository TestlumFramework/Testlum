package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.TokenInput;
import com.knubisoft.testlum.testing.model.scenario.TokenInputPlace;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ANSI_RED_BOLD;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.VALUE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.INPUT_VALUE;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.TOKEN_INPUT_LOCATOR;

@Slf4j
@ExecutorForClass(TokenInput.class)
public class TokenInputExecutor extends AbstractUiExecutor<TokenInput> {

    public TokenInputExecutor(ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final TokenInput tokenInput, final CommandResult result) {
        String parentLocator = tokenInput.getParentLocator();
        result.put(TOKEN_INPUT_LOCATOR, parentLocator);
        String[] splittedToken = tokenInput.getToken().split("");
        List<TokenInputPlace> tokenInputPlaceList = tokenInput.getTokenInputPlace();
        List<WebElement> inputList = getWebElements(tokenInput, tokenInputPlaceList);
        sendInputToElements(inputList, splittedToken, result, tokenInput.isHighlight());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private List<WebElement> getWebElements(final TokenInput tokenInput,
                                            final List<TokenInputPlace> tokenInputPlaceList) {
        if (tokenInputPlaceList.isEmpty()) {
            return UiUtil.findWebElement(dependencies, tokenInput.getParentLocator(), tokenInput.getLocatorStrategy())
                    .findElements(By.xpath("//input[@type='text']"));
        } else {
            return tokenInputPlaceList.stream()
                    .map(input -> UiUtil.findWebElement(dependencies, input.getLocator(), input.getLocatorStrategy()))
                    .collect(Collectors.toList());
        }
    }

    private void sendInputToElements(final List<WebElement> inputList,
                                     final String[] splittedToken,
                                     final CommandResult result,
                                     final boolean highlight) {
        validateInputList(inputList, splittedToken);
        inputList.forEach(inputElement -> {
            int tokenIndex = inputList.indexOf(inputElement);
            sendInputToWebElement(inputElement, splittedToken[tokenIndex], result, highlight);
        });
    }

    private void validateInputList(final List<WebElement> inputList,
                                   final String[] splittedToken) {
        if (splittedToken.length != inputList.size()) {
            throw new IllegalArgumentException(ANSI_RED_BOLD + "Different token length and input fields amount: "
                    + splittedToken.length + " != " + inputList.size());
        }
    }

    private void sendInputToWebElement(final WebElement webElement,
                                       final String inputValue,
                                       final CommandResult result,
                                       final boolean isHighlight) {
        UiUtil.waitForElementVisibility(dependencies, webElement);
        UiUtil.highlightElementIfRequired(isHighlight, webElement, dependencies.getDriver());
        String value = UiUtil.resolveSendKeysType(inputValue, webElement, dependencies.getFile());
        result.put(INPUT_VALUE, value);
        log.info(VALUE_LOG, value);
        webElement.sendKeys(value);
    }
}
