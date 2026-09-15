package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Email;
import lombok.extern.slf4j.Slf4j;

/**
 * Executor for executing email inbox commands within UI blocks (web, native, mobilebrowser).
 * Polls for matching incoming emails and stores extracted values in ScenarioContext.
 */
@Slf4j
@ExecutorForClass(Email.class)
public class EmailExecutor extends AbstractEmailExecutor<Email> {

    /**
     * Constructs EmailExecutor with UI executor dependencies.
     *
     * @param dependencies UI executor runtime dependencies
     */
    public EmailExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void execute(final Email email, final CommandResult result) {
        this.fetchAndProcessEmail(email.getAlias(), email.getPattern(),
                email.getTimeout(), email.getTargetVariable(), result);
    }
}
