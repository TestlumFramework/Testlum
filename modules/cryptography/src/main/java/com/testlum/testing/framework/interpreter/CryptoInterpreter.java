package com.testlum.testing.framework.interpreter;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.interpreter.lib.cryptography.CryptographyService;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.global_config.Cryptography;
import com.testlum.testing.model.scenario.Crypto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
@InterpreterForClass(Crypto.class)
public class CryptoInterpreter extends AbstractInterpreter<Crypto> {

    private static final String NAME_LOG = LogFormat.table("Name");
    private static final String ACTION_LOG = LogFormat.table("Action");
    private static final String ALIAS_LOG = LogFormat.table("Alias");
    private static final String VALUE_LOG = LogFormat.table("Value");

    private final CryptographyService cryptographyService;

    @Autowired(required = false)
    private Map<AliasEnv, Cryptography> cryptographyIntegrations;

    public CryptoInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.cryptographyService = dependencies.getContext().getBean(CryptographyService.class);
    }

    @Override
    protected void acceptImpl(final Crypto o, final CommandResult result) {
        final Crypto crypto = injectCommand(o);
        ensureAlias(crypto::getAlias, crypto::setAlias);
        try {
            CryptographyParams params = fetchCryptographyParams(crypto.getAlias());
            String processedValue = cryptographyService.processCommand(
                    crypto.getValue().trim(), crypto.getAction(),
                    params.method(), params.secret(), crypto.getAlias());
            dependencies.getScenarioContext().set(crypto.getName(), processedValue);
            logCryptographyInfo(crypto.getName(), crypto.getAction(), crypto.getAlias());
        } catch (final Exception e) {
            log.error(ExceptionMessage.FAILED_CRYPTO_LOG, crypto.getName(), crypto.getAction());
            throw e;
        }
    }

    private CryptographyParams fetchCryptographyParams(final String alias) {
        final AliasEnv aliasEnv = new AliasEnv(alias, dependencies.getEnvironment());

        if (cryptographyIntegrations == null || !cryptographyIntegrations.containsKey(aliasEnv)) {
            throw new DefaultFrameworkException(
                    String.format(ExceptionMessage.CRYPTO_NOT_CONFIGURED, alias, dependencies.getEnvironment())
            );
        }

        final Cryptography cryptography = cryptographyIntegrations.get(aliasEnv);
        return new CryptographyParams(cryptography.getMethod().value(), cryptography.getSecret());
    }

    private void logCryptographyInfo(final String name, final String action,
                                     final String alias) {
        log.info(NAME_LOG, name);
        log.info(ACTION_LOG, action);
        log.info(ALIAS_LOG, alias);
        log.info(VALUE_LOG, "*******");
    }
}

record CryptographyParams(
        String method,
        String secret) {

}
