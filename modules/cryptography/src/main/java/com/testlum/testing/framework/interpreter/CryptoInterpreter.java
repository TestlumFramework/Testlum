package com.testlum.testing.framework.interpreter;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.exception.IncorrectCryptographyValueException;
import com.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.interpreter.lib.cryptography.CryptographyService;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.IntegrationsProvider;
import com.testlum.testing.model.global_config.Cryptography;
import com.testlum.testing.model.scenario.Crypto;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@InterpreterForClass(Crypto.class)
public class CryptoInterpreter extends AbstractInterpreter<Crypto> {

    private static final String NAME_LOG = LogFormat.table("Name");
    private static final String ACTION_LOG = LogFormat.table("Action");
    private static final String ALIAS_LOG = LogFormat.table("Alias");
    private static final String VALUE_LOG = LogFormat.table("Value");

    private static final String FAILED_CRYPTO_LOG =
            "Failed crypto operation for name <{}>, action <{}>";
    private static final String INCORRECT_VALUE_MSG =
            "Value must not be empty.";

    private final CryptographyService cryptographyService;
    private final IntegrationsProvider integrationsProvider;

    public CryptoInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.cryptographyService = dependencies.getContext().getBean(CryptographyService.class);
        this.integrationsProvider = dependencies.getContext().getBean(IntegrationsProvider.class);
    }

    @Override
    protected void acceptImpl(final Crypto o, final CommandResult result) {
        final Crypto crypto = injectCommand(o);
        ensureAlias(crypto::getAlias, crypto::setAlias);
        try {
            final String rawValue = validateAndGetValue(crypto);
            CryptographyDto dto = getCryptographyMethodAndSecret(crypto.getAlias());
            String processedValue = cryptographyService.processCommand(rawValue,
                    crypto.getAction(), dto.method(), dto.secret(), crypto.getAlias());
            dependencies.getScenarioContext().set(crypto.getName(), processedValue);
            logCryptographyInfo(crypto.getName(), crypto.getAction(), crypto.getAlias());
        } catch (final Exception e) {
            log.error(FAILED_CRYPTO_LOG, crypto.getName(), crypto.getAction());
            throw e;
        }
    }

    private String validateAndGetValue(final Crypto o) {
        if (o.getValue() == null || o.getValue().trim().isEmpty()) {
            throw new IncorrectCryptographyValueException(INCORRECT_VALUE_MSG);
        }
        return o.getValue().trim();
    }

    private CryptographyDto getCryptographyMethodAndSecret(final String alias) {
        List<Cryptography> cryptographyList =
                integrationsProvider.findListByEnv(Cryptography.class, dependencies.getEnvironment());

        Cryptography cryptographyIntegration =
                integrationsProvider.findCryptographyForAlias(cryptographyList, alias);
        return new CryptographyDto(
                cryptographyIntegration.getMethod().value(),
                cryptographyIntegration.getSecret()
        );
    }

    private void logCryptographyInfo(final String name, final String action,
                                     final String alias) {
        log.info(NAME_LOG, name);
        log.info(ACTION_LOG, action);
        log.info(ALIAS_LOG, alias);
        log.info(VALUE_LOG, "*******");
    }
}

record CryptographyDto(
        String method,
        String secret) {

}
