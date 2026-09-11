package com.testlum.testing.framework.interpreter;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.service.EmailInboxService;
import com.testlum.testing.model.scenario.Email;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * Interpreter for executing standalone scenario-level email inbox commands.
 * Polls for incoming emails matching the configured pattern and stores extracted values in ScenarioContext.
 */
@Slf4j
@InterpreterForClass(Email.class)
public class EmailInterpreter extends AbstractInterpreter<Email> {

    private static final String PATTERN_LOG = LogFormat.table("Pattern");
    private static final String TIMEOUT_LOG = LogFormat.table("Timeout");
    private static final String TARGET_VARIABLE_LOG = LogFormat.table("Target Variable");
    private static final String EXTRACTED_VALUE_LOG = LogFormat.table("Extracted Value");

    private static final String PATTERN = "Pattern";
    private static final String TIMEOUT = "Timeout";
    private static final String TARGET_VARIABLE = "Target Variable";
    private static final String EXTRACTED_VALUE = "Extracted Value";
    private static final String EMAIL_NOT_FOUND_FOR_ALIAS_AND_ENV =
            "Email inbox configuration not found for alias '%s' and environment '%s'";

    private final Map<AliasEnv, EmailInboxService> emailInboxServices;

    /**
     * Constructs EmailInterpreter with runtime dependencies and resolves email inbox services.
     *
     * @param dependencies interpreter runtime dependencies
     */
    @SuppressWarnings("unchecked")
    public EmailInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.emailInboxServices = dependencies.getOptionalBean(
                "emailInboxServices", Map.class, Collections::emptyMap);
    }

    @Override
    protected void acceptImpl(final Email o, final CommandResult result) {
        final Email email = this.injectCommand(o);
        this.ensureAlias(email::getAlias, email::setAlias);
        final EmailInboxService service = this.getEmailInboxService(email.getAlias());
        final String extracted = service.fetchValueByPattern(
                email.getPattern(), email.getTimeout().longValue());
        this.handleTargetVariable(email.getTargetVariable(), extracted);
        this.logEmailInfo(email, extracted);
        this.addEmailMetaData(email, extracted, result);
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

    private void logEmailInfo(final Email email, final String extracted) {
        log.info(ALIAS_LOG, email.getAlias());
        log.info(PATTERN_LOG, email.getPattern());
        log.info(TIMEOUT_LOG, email.getTimeout());
        if (StringUtils.isNotBlank(email.getTargetVariable())) {
            log.info(TARGET_VARIABLE_LOG, email.getTargetVariable());
        }
        log.info(EXTRACTED_VALUE_LOG, this.stringPrettifier.cut(extracted));
    }

    private void addEmailMetaData(final Email email,
                                  final String extracted,
                                  final CommandResult result) {
        result.put(ALIAS, email.getAlias());
        result.put(PATTERN, email.getPattern());
        result.put(TIMEOUT, email.getTimeout());
        if (StringUtils.isNotBlank(email.getTargetVariable())) {
            result.put(TARGET_VARIABLE, email.getTargetVariable());
        }
        result.put(EXTRACTED_VALUE, extracted);
        result.setActual(extracted);
    }
}
