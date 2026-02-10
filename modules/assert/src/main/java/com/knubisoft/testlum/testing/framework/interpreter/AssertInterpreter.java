package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Assert;
import com.knubisoft.testlum.testing.model.scenario.AssertEqual;
import com.knubisoft.testlum.testing.model.scenario.AssertEquality;
import com.knubisoft.testlum.testing.model.scenario.AssertNotEqual;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.LinkedList;
import java.util.List;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COMMA;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


@Slf4j
@InterpreterForClass(Assert.class)
public class AssertInterpreter extends AbstractInterpreter<Assert> {

    private static final String CONTENT = "Content";
    private static final String STEP_FAILED = "Step failed";
    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ANSI_CYAN = "\u001b[36m";
    private static final String ANSI_RESET = "\u001b[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String COMMAND_LOG = ANSI_CYAN + "------- Command #{} - {} -------" + ANSI_RESET;
    private static final String COMMENT_LOG = format(TABLE_FORMAT, "Comment", "{}");
    private static final String CONTENT_LOG = format(TABLE_FORMAT, "Content", "{}");
    private static final String NEW_LOG_LINE = format("%n%19s| ", EMPTY);
    private static final String EXCEPTION_LOG = ANSI_RED
            + "----------------    EXCEPTION    -----------------"
            + NEW_LOG_LINE + "{}" + NEW_LOG_LINE
            + "--------------------------------------------------" + ANSI_RESET;
    private static final String REGEX_NEW_LINE = "[\\r\\n]";
    private static final String ASSERT_CONTENT_NOT_EQUAL = "Equality content <%s> is not equal.";
    private static final String ASSERT_CONTENT_IS_EQUAL = "Inequality content <%s> is equal.";

    public AssertInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Assert o, final CommandResult result) {
        Assert anAssert = injectCommand(o);
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        anAssert.getEqualOrNotEqual().forEach(action -> {
            processAssertAction(action, subCommandsResult);
        });
        setExecutionResultIfSubCommandsFailed(result);
    }

    private void processAssertAction(final AssertEquality action, final List<CommandResult> subCommandsResult) {
        int commandId = dependencies.getPosition().incrementAndGet();
        CommandResult commandResult = newCommandResultInstance(commandId, action);
        subCommandsResult.add(commandResult);
        logAssertEqualityCommand(action, commandId);
        addAssertEqualityMetaData(action, commandResult);
        if (conditionProvider.isTrue(action.getCondition(), dependencies.getScenarioContext(), commandResult)) {
            processEachAction(action, commandResult);
        }
    }

    private void processEachAction(final AssertEquality action, final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            executeEqualityAction(action);
        } catch (Exception e) {
            logException(e);
            setExceptionResult(result, e);
            checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    private void executeEqualityAction(final AssertEquality action) {
        if (action instanceof AssertEqual) {
            checkContentIsEqual((AssertEqual) action);
        } else {
            checkContentNotEqual((AssertNotEqual) action);
        }
    }

    private void checkContentIsEqual(final AssertEqual equal) {
        if (equal.getContent().stream().distinct().count() != 1) {
            throw new DefaultFrameworkException(String.format(ASSERT_CONTENT_NOT_EQUAL, formatContent(equal)));
        }
    }

    private void checkContentNotEqual(final AssertNotEqual notEqual) {
        List<String> content = notEqual.getContent();
        if (content.stream().distinct().count() == 1) {
            throw new DefaultFrameworkException(String.format(ASSERT_CONTENT_IS_EQUAL, formatContent(notEqual)));
        }
    }

    private String formatContent(final AssertEquality action) {
        return String.join(COMMA, action.getContent());
    }

    private CommandResult newCommandResultInstance(final int number, final AbstractCommand... command) {
        CommandResult commandResult = new CommandResult();
        commandResult.setId(number);
        commandResult.setSuccess(true);
        if (nonNull(command) && command.length > 0) {
            commandResult.setCommandKey(command[0].getClass().getSimpleName());
        }
        return commandResult;
    }

    private void addAssertEqualityMetaData(final AssertEquality action, final CommandResult result) {
        result.setComment(action.getComment());
        result.put(CONTENT, String.join(COMMA, action.getContent()));
    }

    private void setExecutionResultIfSubCommandsFailed(final CommandResult result) {
        List<CommandResult> subCommandsResult = result.getSubCommandsResult();
        if (subCommandsResult.stream().anyMatch(step -> !step.isSkipped() && !step.isSuccess())) {
            Exception exception = subCommandsResult
                    .stream()
                    .filter(subCommand -> !subCommand.isSuccess())
                    .findFirst()
                    .map(CommandResult::getException)
                    .orElseGet(() -> new DefaultFrameworkException(STEP_FAILED));
            setExceptionResult(result, exception);
        }
    }

    private void setExceptionResult(final CommandResult result, final Exception exception) {
        result.setSuccess(false);
        result.setException(exception);
    }

    private void logAssertEqualityCommand(final AssertEquality command, final int position) {
        log.info(COMMAND_LOG, position, command.getClass().getSimpleName());
        log.info(COMMENT_LOG, command.getComment());
        log.info(CONTENT_LOG, String.join(COMMA, command.getContent()));
    }

    public void logException(final Exception ex) {
        if (isNotBlank(ex.getMessage())) {
            log.error(EXCEPTION_LOG, ex.getMessage().replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE));
        } else {
            log.error(EXCEPTION_LOG, ex.toString());
        }
    }
}
