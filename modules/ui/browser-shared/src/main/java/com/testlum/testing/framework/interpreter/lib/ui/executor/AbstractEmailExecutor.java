package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.service.EmailExecutionService;
import com.testlum.testing.framework.service.EmailInboxService;
import com.testlum.testing.model.scenario.AbstractUiCommand;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Base executor for UI commands that interact with email inboxes.
 *
 * @param <T> command type
 */
public abstract class AbstractEmailExecutor<T extends AbstractUiCommand> extends AbstractUiExecutor<T> {

    protected final EmailExecutionService emailExecutionService;
    protected final Map<AliasEnv, EmailInboxService> emailInboxServices;

    /**
     * Constructs AbstractEmailExecutor with UI executor dependencies.
     *
     * @param dependencies UI executor runtime dependencies
     */
    @SuppressWarnings("unchecked")
    public AbstractEmailExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.emailExecutionService = dependencies.getContext().getBean(EmailExecutionService.class);
        this.emailInboxServices = dependencies.getContext().containsBean("emailInboxServices")
                ? dependencies.getContext().getBean("emailInboxServices", Map.class)
                : Collections.emptyMap();
    }

    protected String fetchAndProcessEmail(final String alias,
                                         final String pattern,
                                         final BigInteger timeout,
                                         final String targetVariable,
                                         final CommandResult result) {
        final String resolvedAlias = Optional.ofNullable(alias).orElse(EmailExecutionService.DEFAULT_ALIAS);
        final EmailInboxService service = this.emailExecutionService.resolveService(
                this.emailInboxServices, resolvedAlias, this.dependencies.getEnvironment());
        final String extracted = service.fetchValueByPattern(pattern, timeout.longValue());
        this.emailExecutionService.handleTargetVariable(
                this.dependencies.getScenarioContext(), targetVariable, extracted);
        this.emailExecutionService.logEmailInfo(resolvedAlias, pattern, timeout, targetVariable, extracted);
        this.emailExecutionService.addEmailMetaData(
                result, resolvedAlias, pattern, timeout, targetVariable, extracted);
        return extracted;
    }
}
