package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
public abstract class AbstractMessageBrokerInterpreter<T extends AbstractCommand> extends AbstractInterpreter<T> {

    protected static final String SEND_ACTION = "send";
    protected static final String RECEIVE_ACTION = "receive";

    protected static final String ALIAS = "Alias";
    protected static final String ACTION = "Action";
    protected static final String SEND = "Send";
    protected static final String RECEIVE = "Receive";
    protected static final String ENABLE = "Enable";
    protected static final String DISABLE = "Disable";
    protected static final String MESSAGE_TO_SEND = "Message to send";
    protected static final String HEADERS_STATUS = "Headers status";
    protected static final String ADDITIONAL_HEADERS = "Additional headers";
    protected static final String TIMEOUT_MILLIS = "Timeout millis";
    protected static final String HEADER_TEMPLATE = "%s: %s";

    private static final String ACTION_LOG = LogFormat.table("Action");
    private static final String ALIAS_LOG = LogFormat.table("Alias");
    private static final String CONTENT_LOG = LogFormat.table("Content");
    private static final String STEP_FAILED = "Step failed";

    protected AbstractMessageBrokerInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    protected abstract String getAlias(T command);

    protected abstract void setAlias(T command, String alias);

    protected abstract List<Object> getActions(T command);

    protected abstract void processAction(Object action, String alias, CommandResult result);

    @Override
    protected void acceptImpl(final T o, final CommandResult result) {
        T command = injectCommand(o);
        ensureAlias(() -> getAlias(command), alias -> setAlias(command, alias));
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        for (Object action : getActions(command)) {
            log.info(LogFormat.commandLog(),
                    dependencies.getPosition().incrementAndGet(),
                    action.getClass().getSimpleName());
            CommandResult commandResult = newCommandResultInstance(dependencies.getPosition().get());
            subCommandsResult.add(commandResult);
            processEachAction(action, getAlias(command), commandResult);
        }
        setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachAction(final Object action, final String alias, final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        log.info(ALIAS_LOG, alias);
        try {
            processAction(action, alias, result);
        } catch (Exception e) {
            logException(e);
            setExceptionResult(result, e);
            checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getDuration().toMillis());
            stopWatch.stop();
        }
    }

    protected CommandResult newCommandResultInstance(final int number) {
        CommandResult commandResult = new CommandResult();
        commandResult.setId(number);
        commandResult.setSuccess(true);
        return commandResult;
    }

    protected void setExecutionResultIfSubCommandsFailed(final CommandResult result) {
        List<CommandResult> subCommandsResult = result.getSubCommandsResult();
        if (subCommandsResult.stream().anyMatch(step -> !step.isSkipped() && !step.isSuccess())) {
            Exception exception = subCommandsResult.stream()
                    .filter(subCommand -> !subCommand.isSuccess())
                    .findFirst()
                    .map(CommandResult::getException)
                    .orElseGet(() -> new DefaultFrameworkException(STEP_FAILED));
            setExceptionResult(result, exception);
        }
    }

    protected void logIfNotNull(final String title, final Object data) {
        if (Objects.nonNull(data)) {
            log.info(title, data);
        }
    }

    protected String getValue(final String message, final String file) {
        return StringUtils.isNotBlank(message) ? message : getContentIfFile(file);
    }

    protected void logMessageBrokerMetaData(final String action,
                                            final String destinationLogKey,
                                            final String destination,
                                            final String content) {
        log.info(ACTION_LOG, action.toUpperCase(Locale.ROOT));
        log.info(destinationLogKey, destination);
        log.info(CONTENT_LOG, stringPrettifier.asJsonResult(
                        content.replaceAll(DelimiterConstant.REGEX_MANY_SPACES, DelimiterConstant.SPACE))
                .replaceAll(LogFormat.newLine(), LogFormat.contentFormat()));
    }

    protected void addMessageBrokerGeneralMetaData(final String alias,
                                                   final String action,
                                                   final String destinationKey,
                                                   final String destinationValue,
                                                   final CommandResult result) {
        result.put(ALIAS, alias);
        result.put(ACTION, action);
        result.put(destinationKey, destinationValue);
    }

    protected <M> void compareMessages(final List<M> actualMessages,
                                       final String expectedValue,
                                       final CommandResult result) {
        CompareBuilder comparator = newCompare()
                .withExpected(expectedValue)
                .withActual(actualMessages);
        result.setActual(stringPrettifier.asJsonResult(toString(actualMessages)));
        result.setExpected(stringPrettifier.asJsonResult(comparator.getExpected()));
        comparator.exec();
    }
}
