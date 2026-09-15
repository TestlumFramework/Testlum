package com.testlum.testing.framework.service;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.StringPrettifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

/**
 * Shared helper component for email execution logic across interpreters and executors.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailHelper {

    public static final String DEFAULT_ALIAS = "DEFAULT";
    public static final String ALIAS = "Alias";
    public static final String PATTERN = "Pattern";
    public static final String TIMEOUT = "Timeout";
    public static final String TARGET_VARIABLE = "Target Variable";
    public static final String EXTRACTED_VALUE = "Extracted Value";

    public static final String ALIAS_LOG = LogFormat.table(ALIAS);
    public static final String PATTERN_LOG = LogFormat.table(PATTERN);
    public static final String TIMEOUT_LOG = LogFormat.table(TIMEOUT);
    public static final String TARGET_VARIABLE_LOG = LogFormat.table(TARGET_VARIABLE);
    public static final String EXTRACTED_VALUE_LOG = LogFormat.table(EXTRACTED_VALUE);

    public static final String EMAIL_NOT_FOUND_FOR_ALIAS_AND_ENV =
            "Email inbox configuration not found for alias '%s' and environment '%s'";

    private final StringPrettifier stringPrettifier;

    /**
     * Resolves the EmailInboxService from the services map for the given alias and environment.
     *
     * @param services map of configured email inbox services
     * @param alias target inbox alias
     * @param env runtime environment
     * @return found EmailInboxService
     */
    public EmailInboxService resolveService(final Map<AliasEnv, EmailInboxService> services,
                                            final String alias,
                                            final String env) {
        final AliasEnv aliasEnv = new AliasEnv(alias, env);
        return Optional.ofNullable(services)
                .map(map -> map.get(aliasEnv))
                .orElseThrow(() -> new DefaultFrameworkException(EMAIL_NOT_FOUND_FOR_ALIAS_AND_ENV, alias, env));
    }

    /**
     * Saves extracted value to scenario context if target variable is specified.
     *
     * @param context scenario execution context
     * @param targetVariable variable name to store result in
     * @param extracted value extracted from email
     */
    public void handleTargetVariable(final ScenarioContext context,
                                     final String targetVariable,
                                     final String extracted) {
        Optional.ofNullable(targetVariable)
                .filter(StringUtils::isNotBlank)
                .ifPresent(var -> context.set(var, extracted));
    }

    /**
     * Logs email command details.
     *
     * @param alias inbox alias
     * @param pattern search regex pattern
     * @param timeout polling timeout
     * @param targetVariable target variable name
     * @param extracted extracted value
     */
    public void logEmailInfo(final String alias,
                             final String pattern,
                             final BigInteger timeout,
                             final String targetVariable,
                             final String extracted) {
        log.info(ALIAS_LOG, alias);
        log.info(PATTERN_LOG, pattern);
        log.info(TIMEOUT_LOG, timeout);
        Optional.ofNullable(targetVariable)
                .filter(StringUtils::isNotBlank)
                .ifPresent(var -> log.info(TARGET_VARIABLE_LOG, var));
        log.info(EXTRACTED_VALUE_LOG, this.stringPrettifier.cut(extracted));
    }

    /**
     * Populates command result with email metadata.
     *
     * @param result command result
     * @param alias inbox alias
     * @param pattern search regex pattern
     * @param timeout polling timeout
     * @param targetVariable target variable name
     * @param extracted extracted value
     */
    public void addEmailMetaData(final CommandResult result,
                                 final String alias,
                                 final String pattern,
                                 final BigInteger timeout,
                                 final String targetVariable,
                                 final String extracted) {
        result.put(ALIAS, alias);
        result.put(PATTERN, pattern);
        result.put(TIMEOUT, timeout);
        Optional.ofNullable(targetVariable)
                .filter(StringUtils::isNotBlank)
                .ifPresent(var -> result.put(TARGET_VARIABLE, var));
        result.put(EXTRACTED_VALUE, extracted);
        result.setActual(extracted);
    }
}
