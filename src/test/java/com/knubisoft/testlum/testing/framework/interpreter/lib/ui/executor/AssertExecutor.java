package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.AssertAttribute;
import com.knubisoft.testlum.testing.model.scenario.AssertTitle;
import com.knubisoft.testlum.testing.model.scenario.WebAssert;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ASSERT_TYPE_NOT_SUPPORTED;

@ExecutorForClass(WebAssert.class)
public class AssertExecutor extends AbstractUiExecutor<WebAssert> {

    private final Map<AssertCmdPredicate, AssertMethod> assertCommandMap;
    private final List<String> exceptionResult = new ArrayList<>();
    private final AtomicInteger commandId = new AtomicInteger();

    public AssertExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        Map<AssertCmdPredicate, AssertMethod> assertCommands = new HashMap<>();
        assertCommands.put(a -> a instanceof AssertAttribute,
                (a, result) -> executeAttributeCommand((AssertAttribute) a, result));
        assertCommands.put(a -> a instanceof AssertTitle, (a, result) -> executeTitleCommand((AssertTitle) a, result));
        assertCommandMap = Collections.unmodifiableMap(assertCommands);
    }

    @Override
    public void execute(final WebAssert webAssert, final CommandResult result) {
        List<CommandResult> subCommandsResult = new ArrayList<>();
        result.setSubCommandsResult(subCommandsResult);
        webAssert.getAttributeOrTitle().forEach(command -> {
            CommandResult commandResult = ResultUtil.newUiCommandResultInstance(commandId.incrementAndGet(), command);
            subCommandsResult.add(commandResult);
            LogUtil.logAssertCommand(command, commandId.get());
            if (ConditionUtil.isTrue(command.getCondition(), dependencies.getScenarioContext(), commandResult)) {
                executeSubCommand(command, commandResult);
            }
        });
        rethrowOnErrors();
    }

    private void executeSubCommand(final AbstractUiCommand command, final CommandResult result) {
        assertCommandMap.entrySet().stream()
                .filter(method -> method.getKey().test(command))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(ASSERT_TYPE_NOT_SUPPORTED,
                        command.getClass().getSimpleName()))
                .getValue().accept(command, result);
    }

    private void executeAttributeCommand(final AssertAttribute attribute, final CommandResult result) {
        LogUtil.logAssertAttributeInfo(attribute);
        ResultUtil.addAssertAttributeMetaData(attribute, result);
        String actual = getActualValue(attribute);
        String expected = attribute.getContent();
        ResultUtil.setExpectedActual(expected, actual, result);
        executeComparison(actual, expected, result);
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private String getActualValue(final AssertAttribute attribute) {
        WebElement webElement = UiUtil.findWebElement(dependencies, attribute.getLocatorId());
        return UiUtil.getElementAttribute(webElement, attribute.getName(), dependencies.getDriver());
    }

    private void executeTitleCommand(final AssertTitle title, final CommandResult result) {
        LogUtil.logAssertTitleCommand(title);
        String actual = dependencies.getDriver().getTitle();
        ResultUtil.setExpectedActual(title.getContent(), actual, result);
        executeComparison(actual, title.getContent(), result);
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void executeComparison(final String actual, final String expected, final CommandResult result) {
        try {
            new CompareBuilder(dependencies.getFile(), dependencies.getPosition().get())
                    .withActual(actual)
                    .withExpected(expected)
                    .exec();
        } catch (Exception e) {
            handleException(e, result);
        }
    }

    private void handleException(final Exception e, final CommandResult result) {
        LogUtil.logException(e);
        ResultUtil.setExceptionResult(result, e);
        exceptionResult.add(e.getMessage());
    }

    private void rethrowOnErrors() {
        if (!exceptionResult.isEmpty()) {
            throw new DefaultFrameworkException(exceptionResult);
        }
    }

    private interface AssertCmdPredicate extends Predicate<AbstractUiCommand> { }
    private interface AssertMethod extends BiConsumer<AbstractUiCommand, CommandResult> { }
}
