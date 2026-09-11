package com.testlum.testing.framework.interpreter;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.exception.IncorrectCryptographyValueException;
import com.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Crypto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@InterpreterForClass(Crypto.class)
public class CryptoInterpreter extends AbstractInterpreter<Crypto> {

    private static final String NAME_LOG = LogFormat.table("Name");
    private static final String ACTION_LOG = LogFormat.table("Action");
    private static final String ALIAS_LOG = LogFormat.table("Alias");

    private static final String FAILED_CRYPTO_LOG =
            "Failed crypto operation for name <{}>, action <{}>";
    private static final String INCORRECT_VALUE_MSG =
            "The value must be a single, continuous string and must not contain any spaces.";

    public CryptoInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Crypto o, final CommandResult result) {
        final Crypto crypto = injectCommand(o);
        ensureAlias(crypto::getAlias, crypto::setAlias);
        try {
            final String rawValue = validateAndGetValue(crypto);

            // String processedValue =
            // cryptoService.processCommand(crypto.getAction(), crypto.getAlias(), rawValue);

            dependencies.getScenarioContext().set(crypto.getName(), rawValue);
            logCryptographyInfo(crypto.getName(), crypto.getAction(), crypto.getAlias());
        } catch (final Exception e) {
            log.error(FAILED_CRYPTO_LOG, crypto.getName(), crypto.getAction());
            throw e;
        }
    }

    private String validateAndGetValue(final Crypto o) {
        String value = o.getValue().trim();

        if (value.isEmpty() || value.matches(".*\\s.*")) {
            throw new IncorrectCryptographyValueException(INCORRECT_VALUE_MSG);
        }

        return value;
    }

    private void logCryptographyInfo(final String name, final String action, final String alias) {
        log.info(NAME_LOG, name);
        log.info(ACTION_LOG, action);
        log.info(ALIAS_LOG, alias);
    }
}
