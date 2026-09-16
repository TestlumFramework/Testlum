package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.service.EmailExecutionService;
import com.testlum.testing.framework.service.EmailInboxService;
import com.testlum.testing.model.scenario.Email;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;

/**
 * Interpreter for executing standalone scenario-level email inbox commands.
 * Polls for incoming emails matching the configured pattern and stores extracted values in ScenarioContext.
 */
@Slf4j
@InterpreterForClass(Email.class)
public class EmailInterpreter extends AbstractInterpreter<Email> {

    private final EmailExecutionService emailExecutionService;
    private final Map<AliasEnv, EmailInboxService> emailInboxServices;

    /**
     * Constructs EmailInterpreter with runtime dependencies and resolves email inbox services.
     *
     * @param dependencies interpreter runtime dependencies
     */
    @SuppressWarnings("unchecked")
    public EmailInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.emailExecutionService = dependencies.getContext().getBean(EmailExecutionService.class);
        this.emailInboxServices = dependencies.getOptionalBean(
                "emailInboxServices", Map.class, Collections::emptyMap);
    }

    @Override
    protected void acceptImpl(final Email o, final CommandResult result) {
        final Email email = this.injectCommand(o);
        this.ensureAlias(email::getAlias, email::setAlias);
        final EmailInboxService service = this.emailExecutionService.resolveService(
                this.emailInboxServices, email.getAlias(), this.dependencies.getEnvironment());
        final String extracted = service.fetchValueByPattern(
                email.getPattern(), email.getTimeout().longValue());
        this.emailExecutionService.handleTargetVariable(
                this.dependencies.getScenarioContext(), email.getTargetVariable(), extracted);
        this.emailExecutionService.logEmailInfo(email.getAlias(), email.getPattern(), email.getTimeout(),
                email.getTargetVariable(), extracted);
        this.emailExecutionService.addEmailMetaData(result, email.getAlias(), email.getPattern(), email.getTimeout(),
                email.getTargetVariable(), extracted);
    }
}
