package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
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
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.ASSERT_TYPE_NOT_SUPPORTED;

@ExecutorForClass(Assert.class)
public class AssertExecutor extends AbstractUiExecutor<Assert> {

    private final Map<AssertCmdPredicate, AssertMethod> assertCommandMap;
    private final List<String> exceptionResult = new ArrayList<>();
    private final AtomicInteger commandId = new AtomicInteger();

    public AssertExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        Map<AssertCmdPredicate, AssertMethod> assertCommands = new HashMap<>();
        assertCommands.put(a -> a instanceof Attribute, (a, result) -> executeAttributeCommand((Attribute) a, result));
        assertCommands.put(a -> a instanceof Title, (a, result) -> executeTitleCommand((Title) a, result));
        assertCommandMap = Collections.unmodifiableMap(assertCommands);
    }

    @Override
    public void execute(final Assert aAssert, final CommandResult result) {
        List<CommandResult> subCommandsResult = new ArrayList<>();
        result.setSubCommandsResult(subCommandsResult);
        aAssert.getAttributeOrTitle().forEach(command -> {
            CommandResult subCommandResult = createSubCommandResult(command);
            subCommandsResult.add(subCommandResult);
            executeSubCommand(command, subCommandResult);
        });
        rethrowOnErrors();
    }

    private void executeSubCommand(final AbstractUiCommand command, final CommandResult result) {
        assertCommandMap.keySet().stream()
                .filter(cmd -> cmd.test(command))
                .findFirst()
                .map(assertCommandMap::get)
                .orElseThrow(() -> new DefaultFrameworkException(ASSERT_TYPE_NOT_SUPPORTED,
                        command.getClass().getSimpleName()))
                .accept(command, result);
    }

    private void executeAttributeCommand(final Attribute attribute, final CommandResult result) {
        injectFields(attribute);
        LogUtil.logAssertAttributeInfo(attribute, commandId.get());
        ResultUtil.addAssertAttributeMetaData(attribute, result);
        try {
            String actual = getActualValue(attribute);
            String expected = attribute.getContent().replaceAll(SPACE, EMPTY).replaceAll(NEW_LINE, EMPTY);
            executeComparison(actual, expected, result);
            UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        } catch (Exception e) {
            handleException(result, e);
        }
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

    private void executeTitleCommand(final Title title, final CommandResult result) {
        title.setContent(inject(title.getContent()));
        LogUtil.logAssertTitleCommand(title, commandId.get());
        try {
            String actual = dependencies.getDriver().getTitle();
            executeComparison(actual, title.getContent(), result);
            UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        } catch (Exception e) {
            handleException(result, e);
        }
    }

    private void executeComparison(final String actual,
                                   final String expected,
                                   final CommandResult result) {
        ResultUtil.setExpectedActual(expected, actual, result);
        new CompareBuilder(dependencies.getFile(), dependencies.getPosition())
                .withActual(actual)
                .withExpected(expected)
                .exec();
    }

    private void handleException(final CommandResult result, final Exception e) {
        LogUtil.logException(e);
        ResultUtil.setExceptionResult(result, e);
        exceptionResult.add(e.getMessage());
    }

    private CommandResult createSubCommandResult(final AbstractUiCommand command) {
        return ResultUtil.createCommandResultForUiSubCommand(
                commandId.incrementAndGet(),
                command.getClass().getSimpleName(),
                command.getComment());
    }

    private void rethrowOnErrors() {
        if (!exceptionResult.isEmpty()) {
            throw new DefaultFrameworkException(exceptionResult);
        }
    }

    private interface AssertCmdPredicate extends Predicate<AbstractUiCommand> { }
    private interface AssertMethod extends BiConsumer<AbstractUiCommand, CommandResult> { }
}
