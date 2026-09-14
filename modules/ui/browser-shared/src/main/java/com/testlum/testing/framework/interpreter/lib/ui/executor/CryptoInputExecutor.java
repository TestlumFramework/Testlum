package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.exception.IncorrectCryptographyValueException;
import com.testlum.testing.framework.interpreter.lib.cryptography.CryptographyService;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.IntegrationsProvider;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.global_config.Cryptography;
import com.testlum.testing.model.scenario.CryptoInput;
import com.testlum.testing.model.scenario.CryptoOperation;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

import java.util.List;

@Slf4j
@ExecutorForClass(CryptoInput.class)
public class CryptoInputExecutor
        extends AbstractUiExecutor<CryptoInput> {

    private static final String FAILED_CRYPTO_INPUT_LOG =
            "Failed crypto input operation for locator <{}>, action <{}>";
    private static final String INCORRECT_VALUE_MSG =
            "Value must not be empty.";
    private static final String MISSING_OPERATION_MSG =
            "Crypto operation (<encrypt> or <decrypt>) is missing in cryptoInput.";

    private final CryptographyService cryptographyService;
    private final IntegrationsProvider integrationsProvider;

    public CryptoInputExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.cryptographyService = dependencies.getContext().getBean(CryptographyService.class);
        this.integrationsProvider = dependencies.getContext().getBean(IntegrationsProvider.class);
    }

    @Override
    public void execute(final CryptoInput cryptoInput, final CommandResult result) {
        result.put(ResultUtil.INPUT_LOCATOR, cryptoInput.getLocator());
        try {
            final String processedValue = processCryptoValue(cryptoInput);
            final WebElement webElement = getAndPrepareElement(cryptoInput);
            sendValueToElement(processedValue, webElement, result);
        } catch (final Exception e) {
            log.error(FAILED_CRYPTO_INPUT_LOG, cryptoInput.getLocator(), cryptoInput.getAction());
            throw e;
        }
    }

    private String processCryptoValue(final CryptoInput cryptoInput) {
        final CryptoOperation operation = validateAndGetOperation(cryptoInput);
        final String rawValue = validateAndGetValue(operation);
        final CryptographyDto dto = getCryptographyMethodAndSecret(operation.getAlias());
        return cryptographyService.processCommand(
                rawValue, cryptoInput.getAction(), dto.method(), dto.secret(), operation.getAlias()
        );
    }

    private WebElement getAndPrepareElement(final CryptoInput cryptoInput) {
        final WebElement element = uiUtil.findWebElement(
                dependencies, cryptoInput.getLocator(), cryptoInput.getLocatorStrategy(), ElementChecks.FOR_WRITING
        );
        uiUtil.highlightElementIfRequired(cryptoInput.isHighlight(), element, dependencies.getDriver());
        return element;
    }

    private void sendValueToElement(final String processedValue, final WebElement element, final CommandResult result) {
        final String valueToSend = uiUtil.resolveSendKeysType(processedValue, element, dependencies.getFile());
        result.put(ResultUtil.INPUT_VALUE, valueToSend);
        log.info(LogMessage.VALUE_LOG, valueToSend);
        element.sendKeys(valueToSend);
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private CryptoOperation validateAndGetOperation(final CryptoInput cryptoInput) {
        final CryptoOperation operation = cryptoInput.getOperation();
        if (operation == null) {
            throw new DefaultFrameworkException(MISSING_OPERATION_MSG);
        }
        return operation;
    }

    private String validateAndGetValue(final CryptoOperation operation) {
        if (operation.getValue() == null || operation.getValue().trim().isEmpty()) {
            throw new IncorrectCryptographyValueException(INCORRECT_VALUE_MSG);
        }
        return operation.getValue().trim();
    }

    private CryptographyDto getCryptographyMethodAndSecret(final String alias) {
        final List<Cryptography> list = integrationsProvider
                .findListByEnv(Cryptography.class, dependencies.getEnvironment());
        final Cryptography cryptography = integrationsProvider.findCryptographyForAlias(list, alias);
        return new CryptographyDto(cryptography.getMethod().value(), cryptography.getSecret());
    }
}

record CryptographyDto(
        String method,
        String secret) {

}