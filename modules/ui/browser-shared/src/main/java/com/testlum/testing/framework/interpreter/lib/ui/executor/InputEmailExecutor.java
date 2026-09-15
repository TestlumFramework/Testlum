package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.service.EmailInboxService;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.scenario.InputEmail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.Map;

/**
 * Executor for extracting values from email inboxes and typing them directly into UI target elements.
 * Locates the target element, sends the extracted value, and optionally highlights the element.
 */
@Slf4j
@ExecutorForClass(InputEmail.class)
public class InputEmailExecutor extends AbstractUiExecutor<InputEmail> {

    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";
    private static final String ALIAS_LOG = LogFormat.table("Alias");
    private static final String PATTERN_LOG = LogFormat.table("Pattern");
    private static final String TIMEOUT_LOG = LogFormat.table("Timeout");
    private static final String TARGET_VARIABLE_LOG = LogFormat.table("Target Variable");
    private static final String EXTRACTED_VALUE_LOG = LogFormat.table("Extracted Value");

    private static final String ALIAS = "Alias";
    private static final String PATTERN = "Pattern";
    private static final String TIMEOUT = "Timeout";
    private static final String TARGET_VARIABLE = "Target Variable";
    private static final String EXTRACTED_VALUE = "Extracted Value";
    private static final String EMAIL_NOT_FOUND_FOR_ALIAS_AND_ENV =
            "Email inbox configuration not found for alias '%s' and environment '%s'";
    private static final String NON_EDITABLE_ELEMENT_EXCEPTION =
            "Element found by locator '%s' (<%s>) is not an editable field. "
                    + "Expected <input>, <textarea>, or an element with contenteditable='true'";
    private static final String ELEMENT_NOT_INTERACTABLE_EXCEPTION =
            "Cannot type into element found by locator '%s': element is not interactable";
    private static final String FAILED_TO_TYPE_EXCEPTION =
            "Failed to type into element found by locator '%s': %s";

    private final Map<AliasEnv, EmailInboxService> emailInboxServices;

    /**
     * Constructs InputEmailExecutor with UI executor dependencies.
     *
     * @param dependencies UI executor runtime dependencies
     */
    @SuppressWarnings("unchecked")
    public InputEmailExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.emailInboxServices = dependencies.getContext().containsBean("emailInboxServices")
                ? dependencies.getContext().getBean("emailInboxServices", Map.class)
                : Collections.emptyMap();
    }

    @Override
    protected void execute(final InputEmail inputEmail, final CommandResult result) {
        this.checkAlias(inputEmail);
        final EmailInboxService service = this.getEmailInboxService(inputEmail.getAlias());
        final String extracted = service.fetchValueByPattern(
                inputEmail.getPattern(), inputEmail.getTimeout().longValue());
        this.handleTargetVariable(inputEmail.getTargetVariable(), extracted);
        this.typeIntoElement(inputEmail, extracted, result);
        this.logInputEmailInfo(inputEmail, extracted);
        this.addInputEmailMetaData(inputEmail, extracted, result);
    }

    private void checkAlias(final InputEmail inputEmail) {
        if (inputEmail.getAlias() == null) {
            inputEmail.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private EmailInboxService getEmailInboxService(final String alias) {
        final AliasEnv aliasEnv = new AliasEnv(alias, this.dependencies.getEnvironment());
        final EmailInboxService service = this.emailInboxServices.get(aliasEnv);
        if (service == null) {
            throw new DefaultFrameworkException(EMAIL_NOT_FOUND_FOR_ALIAS_AND_ENV,
                    alias, this.dependencies.getEnvironment());
        }
        return service;
    }

    private void handleTargetVariable(final String targetVariable, final String extracted) {
        if (StringUtils.isNotBlank(targetVariable)) {
            this.dependencies.getScenarioContext().set(targetVariable, extracted);
        }
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
        final String contentEditable = webElement.getAttribute("contenteditable");
        return contentEditable != null && !Boolean.FALSE.toString().equalsIgnoreCase(contentEditable);
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

    private void logInputEmailInfo(final InputEmail inputEmail, final String extracted) {
        log.info(ALIAS_LOG, inputEmail.getAlias());
        log.info(PATTERN_LOG, inputEmail.getPattern());
        log.info(TIMEOUT_LOG, inputEmail.getTimeout());
        if (StringUtils.isNotBlank(inputEmail.getTargetVariable())) {
            log.info(TARGET_VARIABLE_LOG, inputEmail.getTargetVariable());
        }
        log.info(EXTRACTED_VALUE_LOG, this.stringPrettifier.cut(extracted));
    }

    private void addInputEmailMetaData(final InputEmail inputEmail,
                                       final String extracted,
                                       final CommandResult result) {
        result.put(ALIAS, inputEmail.getAlias());
        result.put(PATTERN, inputEmail.getPattern());
        result.put(TIMEOUT, inputEmail.getTimeout());
        if (StringUtils.isNotBlank(inputEmail.getTargetVariable())) {
            result.put(TARGET_VARIABLE, inputEmail.getTargetVariable());
        }
        result.put(EXTRACTED_VALUE, extracted);
        result.setActual(extracted);
    }
}
