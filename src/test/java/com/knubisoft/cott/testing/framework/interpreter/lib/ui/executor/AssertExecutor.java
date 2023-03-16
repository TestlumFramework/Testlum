package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.cott.testing.model.scenario.Assert;
import com.knubisoft.cott.testing.model.scenario.Attribute;
import com.knubisoft.cott.testing.model.scenario.Title;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.NEW_LINE;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.RETHROWN_ERRORS_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.ASSERT_ATTRIBUTE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.ASSERT_LOCATOR;

@ExecutorForClass(Assert.class)
public class AssertExecutor extends AbstractUiExecutor<Assert> {

    private final List<String> exceptionResult = new ArrayList<>();

    private final Map<AssertCommandPredicate, AssertMethod> assertCommandMap;

    private AtomicInteger subCommandCounter = new AtomicInteger(0);

    public AssertExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        Map<AssertCommandPredicate, AssertMethod> assertCommands = new HashMap<>();
        assertCommands.put(a -> a instanceof Attribute,
                (a, result) -> executeAttributeCommand((Attribute) a, result));
        assertCommands.put(a -> a instanceof Title,
                (a, result) -> executeTitleCommand((Title) a, result));
        assertCommandMap = Collections.unmodifiableMap(assertCommands);
    }

    @Override
    public void execute(final Assert aAssert, final CommandResult result) {
        List<CommandResult> subCommandResult = new ArrayList<>();
        result.setSubCommandsResult(subCommandResult);
        aAssert.getAttributeOrTitle().forEach(command -> assertCommandMap.keySet().stream()
                .filter(cmd -> cmd.test(command))
                .findFirst()
                .map(assertCommandMap::get)
                .orElseThrow(() -> new DefaultFrameworkException("Type of 'Assert' tag is not supported"))
                .accept(command, subCommandResult));
        rethrowOnErrors();
    }

    private void executeTitleCommand(final Title title, final List<CommandResult> subCommandsResult) {
        String actual = dependencies.getDriver().getTitle();
        String expected = inject(title.getContent()).replaceAll(SPACE, EMPTY).replaceAll(NEW_LINE, EMPTY);
        LogUtil.logTitleCommand(title, subCommandCounter.incrementAndGet());
        CommandResult subCommandResult = createSubCommandResult(title, actual, expected, subCommandsResult);
        executeComparison(actual, expected, subCommandResult);
    }

    private void executeAttributeCommand(final Attribute attribute,
                                         final List<CommandResult> subCommandsResult) {
        injectFields(attribute);
        String actual = getActualValue(attribute);
        String expected = attribute.getContent().replaceAll(SPACE, EMPTY).replaceAll(NEW_LINE, EMPTY);
        LogUtil.logAttributeInfo(attribute, subCommandCounter.incrementAndGet());
        CommandResult subCommandResult = createSubCommandResult(attribute, actual, expected, subCommandsResult);
        subCommandResult.put(ASSERT_LOCATOR, attribute.getLocatorId());
        subCommandResult.put(ASSERT_ATTRIBUTE, attribute.getName());
        executeComparison(actual, expected, subCommandResult);
    }

    private void injectFields(final Attribute attribute) {
        attribute.setLocatorId(inject(attribute.getLocatorId()));
        attribute.setContent(inject(attribute.getContent()));
        attribute.setName(inject(attribute.getName()));
    }

    private String getActualValue(final Attribute attribute) {
        WebElement webElement = UiUtil.findWebElement(dependencies, attribute.getLocatorId());
        UiUtil.waitForElementVisibility(dependencies, webElement);
        String value = UiUtil.getElementAttribute(webElement, attribute.getName());
        return value.replaceAll(SPACE, EMPTY).replaceAll(NEW_LINE, EMPTY);
    }

    private CommandResult createSubCommandResult(final AbstractCommand command,
                                                 final String actual,
                                                 final String expected,
                                                 final List<CommandResult> subCommandsResult) {
        CommandResult subCommandResult = ResultUtil.createCommandResultForUiSubCommand(
                subCommandCounter.get(),
                command.getClass().getSimpleName(),
                command.getComment());
        subCommandResult.setActual(actual);
        subCommandResult.setExpected(expected);
        UiUtil.takeScreenshotAndSaveIfRequired(subCommandResult, dependencies);
        subCommandsResult.add(subCommandResult);
        return subCommandResult;
    }

    private void executeComparison(final String actual,
                                   final String expected,
                                   final CommandResult subCommandResult) {
        try {
            new CompareBuilder(dependencies.getFile(), dependencies.getPosition())
                    .withActual(actual)
                    .withExpected(expected)
                    .exec();
        } catch (Exception e) {
            LogUtil.logException(e);
            exceptionResult.add(e.getMessage());
            ResultUtil.setExceptionResult(subCommandResult, e);
        }
    }

    public void rethrowOnErrors() {
        if (!exceptionResult.isEmpty()) {
            throw new DefaultFrameworkException(RETHROWN_ERRORS_LOG,
                    String.join(DelimiterConstant.SPACE_WITH_LF, exceptionResult));
        }
    }

    private interface AssertCommandPredicate extends Predicate<AbstractUiCommand> { }

    private interface AssertMethod extends BiConsumer<AbstractUiCommand, List<CommandResult>> { }
}
