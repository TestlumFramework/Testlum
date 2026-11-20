package com.testlum.testing.framework.interpreter;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.constant.DelimiterConstant;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(Assert.class)
public class AssertInterpreter extends AbstractInterpreter<Assert> {

    private static final String CONTENT = "Content";
    private static final String PATH = "Path";

    private static final String COMMENT_LOG = LogFormat.table("Comment");
    private static final String CONTENT_LOG = LogFormat.table("Content");
    private static final String PATH_LOG = LogFormat.table("Path");

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
        anAssert.getEqualOrNotEqualOrJsonPath().forEach(action -> {
            processAssertAction(action, subCommandsResult);
        });
        setExecutionResultIfSubCommandsFailed(result);
    }

    private void processAssertAction(final AbstractCommand action, final List<CommandResult> subCommandsResult) {
        int commandId = dependencies.getPosition().incrementAndGet();
        CommandResult commandResult = newCommandResultInstance(commandId, action);
        subCommandsResult.add(commandResult);
        describe(action, commandId, commandResult);
        if (conditionProvider.isTrue(action.getCondition(), dependencies.getScenarioContext(), commandResult)) {
            processEachAction(action, commandResult);
        }
    }

    private void describe(final AbstractCommand action, final int commandId, final CommandResult commandResult) {
        if (action instanceof AssertJsonPath jsonPath) {
            logJsonPathCommand(jsonPath, commandId);
            commandResult.setComment(jsonPath.getComment());
            commandResult.put(PATH, jsonPath.getPath());
        } else {
            AssertEquality equality = (AssertEquality) action;
            logAssertEqualityCommand(equality, commandId);
            addAssertEqualityMetaData(equality, commandResult);
        }
    }

    private void processEachAction(final AbstractCommand action, final CommandResult result) {
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

    private void executeEqualityAction(final AbstractCommand action) {
        if (action instanceof AssertEqual assertEqual) {
            checkContentIsEqual(assertEqual);
        } else if (action instanceof AssertJsonPath jsonPath) {
            checkJsonPath(jsonPath);
        } else {
            checkContentNotEqual((AssertNotEqual) action);
        }
    }

    private void checkJsonPath(final AssertJsonPath jsonPath) {
        String body = jsonPath.getFromVar() != null
                ? dependencies.getScenarioContext().get(jsonPath.getFromVar())
                : dependencies.getScenarioContext().getBody().getValue();
        String failure = JsonPathAssertion.check(body, jsonPath);
        if (failure != null) {
            throw new DefaultFrameworkException(failure);
        }
    }

    private void logJsonPathCommand(final AssertJsonPath command, final int position) {
        log.info(LogFormat.commandLog(), position, command.getClass().getSimpleName());
        log.info(COMMENT_LOG, command.getComment());
        log.info(PATH_LOG, command.getPath());
    }

    private void checkContentIsEqual(final AssertEqual equal) {
        if (equal.getContent().stream()
                    .map(this::normalizeLineEndings)
                    .distinct()
                    .count() != 1) {
            throw new DefaultFrameworkException(String.format(ASSERT_CONTENT_NOT_EQUAL, formatContent(equal)));
        }
    }

    private void checkContentNotEqual(final AssertNotEqual notEqual) {
        List<String> content = notEqual.getContent();
        if (content.stream()
                    .map(this::normalizeLineEndings)
                    .distinct()
                    .count() == 1) {
            throw new DefaultFrameworkException(String.format(ASSERT_CONTENT_IS_EQUAL, formatContent(notEqual)));
        }
    }

    private String formatContent(final AssertEquality action) {
        return String.join(DelimiterConstant.COMMA, action.getContent());
    }

    private CommandResult newCommandResultInstance(final int number, final AbstractCommand... command) {
        CommandResult commandResult = new CommandResult();
        commandResult.setId(number);
        commandResult.setSuccess(true);
        if (Objects.nonNull(command) && command.length > 0) {
            commandResult.setCommandKey(command[0].getClass().getSimpleName());
        }
        return commandResult;
    }

    private void addAssertEqualityMetaData(final AssertEquality action, final CommandResult result) {
        result.setComment(action.getComment());
        result.put(CONTENT, String.join(DelimiterConstant.COMMA, action.getContent()));
    }

    private void logAssertEqualityCommand(final AssertEquality command, final int position) {
        log.info(LogFormat.commandLog(), position, command.getClass().getSimpleName());
        log.info(COMMENT_LOG, command.getComment());
        log.info(CONTENT_LOG, String.join(DelimiterConstant.COMMA, command.getContent()));
    }

    private String normalizeLineEndings(final String content) {
        if (content == null) {
            return null;
        }
        return content.lines()
                .collect(Collectors.joining("\n"));
    }

}
