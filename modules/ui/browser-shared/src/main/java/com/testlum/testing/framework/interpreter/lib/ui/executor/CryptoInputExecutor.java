package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.cryptography.CryptographyService;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.global_config.CryptoMethods;
import com.testlum.testing.model.global_config.Cryptography;
import com.testlum.testing.model.scenario.CryptoInput;
import com.testlum.testing.model.scenario.CryptoOperation;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

@Slf4j
@ExecutorForClass(CryptoInput.class)
public class CryptoInputExecutor
        extends AbstractUiExecutor<CryptoInput> {

    private final CryptographyService cryptographyService;

    @Autowired(required = false)
    private Map<AliasEnv, Cryptography> cryptographyIntegrations;

    public CryptoInputExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.cryptographyService = dependencies.getContext().getBean(CryptographyService.class);
    }

    @Override
    public void execute(final CryptoInput cryptoInput, final CommandResult result) {
        result.put(ResultUtil.INPUT_LOCATOR, cryptoInput.getLocator());

        final String processedValue = processCryptoValue(cryptoInput);
        final WebElement webElement = getAndPrepareElement(cryptoInput);
        sendValueToElement(processedValue, webElement, result);
    }

    private String processCryptoValue(final CryptoInput cryptoInput) {
        final CryptoOperation operation = cryptoInput.getOperation();
        final String rawValue = operation.getValue().trim();
        final CryptographyParams dto = fetchCryptoParams(operation.getAlias());
        return cryptographyService.processCommand(
                rawValue, cryptoInput.getAction(),
                dto.method(), dto.secret(), operation.getAlias()
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

    private CryptographyParams fetchCryptoParams(final String alias) {
        final AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());

        final Cryptography cryptography = Optional.ofNullable(cryptographyIntegrations)
                .map(map -> map.get(aliasEnv))
                .orElseThrow(() -> new DefaultFrameworkException(
                        String.format(ExceptionMessage.CRYPTO_NOT_CONFIGURED, alias, dependencies.getEnvironment())
                ));

        final String method = Optional.ofNullable(cryptography.getMethod())
                .map(CryptoMethods::value)
                .orElseThrow(() -> new DefaultFrameworkException(
                        String.format("Cryptography method is not set for alias <%s>", alias)
                ));

        return new CryptographyParams(method, cryptography.getSecret());
    }
}

record CryptographyParams(
        String method,
        String secret) {

}