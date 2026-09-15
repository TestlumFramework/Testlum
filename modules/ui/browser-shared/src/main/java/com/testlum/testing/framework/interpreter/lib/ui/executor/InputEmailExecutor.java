package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.scenario.InputEmail;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.Optional;

/**
 * Executor for extracting values from email inboxes and typing them directly into UI target elements.
 * Locates the target element, sends the extracted value, and optionally highlights the element.
 */
@Slf4j
@ExecutorForClass(InputEmail.class)
public class InputEmailExecutor extends AbstractEmailExecutor<InputEmail> {

    private static final String NON_EDITABLE_ELEMENT_EXCEPTION =
            "Element found by locator '%s' (<%s>) is not an editable field. "
                    + "Expected <input>, <textarea>, or an element with contenteditable='true'";
    private static final String ELEMENT_NOT_INTERACTABLE_EXCEPTION =
            "Cannot type into element found by locator '%s': element is not interactable";
    private static final String FAILED_TO_TYPE_EXCEPTION =
            "Failed to type into element found by locator '%s': %s";

    /**
     * Constructs InputEmailExecutor with UI executor dependencies.
     *
     * @param dependencies UI executor runtime dependencies
     */
    public InputEmailExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void execute(final InputEmail inputEmail, final CommandResult result) {
        final String extracted = this.fetchAndProcessEmail(inputEmail.getAlias(), inputEmail.getPattern(),
                inputEmail.getTimeout(), inputEmail.getTargetVariable(), result);
        this.typeIntoElement(inputEmail, extracted, result);
    }

    private void typeIntoElement(final InputEmail inputEmail,
                                 final String value,
                                 final CommandResult result) {
        result.put(ResultUtil.INPUT_LOCATOR, inputEmail.getLocator());
        final WebElement webElement = this.uiUtil.findWebElement(this.dependencies,
                inputEmail.getLocator(), inputEmail.getLocatorStrategy(), ElementChecks.FOR_WRITING);
        this.validateTargetElement(webElement, inputEmail.getLocator());
        this.uiUtil.highlightElementIfRequired(inputEmail.isHighlight(), webElement, this.dependencies.getDriver());
        result.put(ResultUtil.INPUT_VALUE, value);
        log.info(LogMessage.VALUE_LOG, value);
        this.sendValue(webElement, value, inputEmail.getLocator());
        this.uiUtil.takeScreenshotAndSaveIfRequired(result, this.dependencies);
    }

    private void validateTargetElement(final WebElement webElement, final String locator) {
        if (this.dependencies.getUiType() == UiType.NATIVE) {
            return;
        }
        final String tagName = webElement.getTagName();
        if (this.isInputOrTextarea(tagName) || this.isContentEditable(webElement)) {
            return;
        }
        throw new DefaultFrameworkException(NON_EDITABLE_ELEMENT_EXCEPTION, locator, tagName);
    }

    private boolean isInputOrTextarea(final String tagName) {
        return "input".equalsIgnoreCase(tagName) || "textarea".equalsIgnoreCase(tagName);
    }

    private boolean isContentEditable(final WebElement webElement) {
        return Optional.ofNullable(webElement.getAttribute("contenteditable"))
                .map(val -> !Boolean.FALSE.toString().equalsIgnoreCase(val))
                .orElse(false);
    }

    private void sendValue(final WebElement webElement, final String value, final String locator) {
        try {
            webElement.sendKeys(value);
        } catch (ElementNotInteractableException e) {
            throw new DefaultFrameworkException(ELEMENT_NOT_INTERACTABLE_EXCEPTION, locator);
        } catch (WebDriverException e) {
            throw new DefaultFrameworkException(FAILED_TO_TYPE_EXCEPTION, locator, e.getMessage());
        }
    }
}
