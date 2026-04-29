package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Slf4j
@ExecutorForClass(WebAssert.class)
public class AssertExecutor extends AbstractUiExecutor<WebAssert> {

    private static final String ASSERT_NOT_PRESENT = "Element with locator <%s> should not be present.";
    private static final String ASSERT_CHECKED = "Element with locator <%s> failed check assertion.";
    private static final String ASSERT_FAILED_EQUAL = "Property [%s] is equal to [%s]";

    private final List<String> exceptions = new ArrayList<>();
    private final Map<AssertCmdPredicate, AssertMethod> assertCommandMap;

    public AssertExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.assertCommandMap = Map.of(
                AssertAttribute.class::isInstance, (a, result) -> executeAttributeCommand((AssertAttribute) a, result),
                a -> a instanceof AssertEquality, (a, result) -> executeEqualityCommand((AssertEquality) a, result),
                AssertTitle.class::isInstance, (a, result) -> executeTitleCommand((AssertTitle) a, result),
                AssertAlert.class::isInstance, (a, result) -> executeAssertAlert((AssertAlert) a, result),
                AssertChecked.class::isInstance, (a, result) -> executeAssertChecked((AssertChecked) a, result),
                AssertPresent.class::isInstance, (a, result) -> executeAssertPresent((AssertPresent) a, result));
    }

    @Override
    public void execute(final WebAssert webAssert, final CommandResult result) {
        List<CommandResult> subCommandsResult = new ArrayList<>();
        result.setSubCommandsResult(subCommandsResult);
        webAssert.getAttributeOrTitleOrEqual().forEach(command -> {
            CommandResult commandResult =
                    resultUtil.newUiCommandResultInstance(dependencies.getPosition().incrementAndGet(), command);
            subCommandsResult.add(commandResult);
            uiLogUtil.logAssertCommand(command, dependencies.getPosition().get());
            if (conditionUtil.isTrue(command.getCondition(), dependencies.getScenarioContext(), commandResult)) {
                executeSubCommand(command, commandResult);
            }
        });
        rethrowOnErrors();
    }

    private void executeSubCommand(final AbstractCommand command, final CommandResult result) {
        assertCommandMap.entrySet().stream()
                .filter(method -> method.getKey().test(command))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.ASSERT_TYPE_NOT_SUPPORTED,
                        command.getClass().getSimpleName()))
                .getValue().accept(command, result);
    }

    private void executeAttributeCommand(final AssertAttribute attribute, final CommandResult result) {
        uiLogUtil.logAssertAttributeInfo(attribute);
        resultUtil.addAssertAttributeMetaData(attribute, result);
        String actual = getActualValue(attribute);
        String expected = attribute.getContent();
        resultUtil.setExpectedActual(expected, actual, result);
        executeComparison(actual, expected, result, attribute.isNegative());
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private String getActualValue(final AssertAttribute attribute) {
        WebElement webElement = uiUtil.findWebElement(dependencies, attribute.getLocator(),
                attribute.getLocatorStrategy());
        return uiUtil.getElementAttribute(webElement, attribute.getName(), dependencies.getDriver());
    }

    private void executeEqualityCommand(final AssertEquality action, final CommandResult result) {
        log.info(LogMessage.CONTENT_LOG, String.join(DelimiterConstant.COMMA, action.getContent()));
        result.put(ResultUtil.CONTENT, String.join(DelimiterConstant.COMMA, action.getContent()));
        if (action instanceof AssertEqual assertEqual) {
            checkContentIsEqual(assertEqual);
        } else {
            checkContentNotEqual((AssertNotEqual) action);
        }
    }

    private void checkContentIsEqual(final AssertEqual equal) {
        AssertEqualityHelper.checkContentIsEqual(equal);
    }

    private void checkContentNotEqual(final AssertNotEqual notEqual) {
        AssertEqualityHelper.checkContentNotEqual(notEqual);
    }

    private void executeTitleCommand(final AssertTitle title, final CommandResult result) {
        uiLogUtil.logAssertTitleCommand(title);
        String actual = dependencies.getDriver().getTitle();
        resultUtil.setExpectedActual(title.getContent(), actual, result);
        executeComparison(actual, title.getContent(), result, title.isNegative());
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void executeAssertAlert(final AssertAlert alert, final CommandResult result) {
        uiLogUtil.logAssertAlertCommand(alert);
        String actual = dependencies.getDriver().switchTo().alert().getText();
        resultUtil.setExpectedActual(alert.getText(), actual, result);
        executeComparison(actual, alert.getText(), result, alert.isNegative());
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void executeAssertPresent(final AssertPresent present, final CommandResult result) {
        try {
            uiLogUtil.logAssertPresent(present);
            resultUtil.addAssertPresentMetadata(present, result);
            uiUtil.findWebElement(dependencies, present.getLocator(), present.getLocatorStrategy());
            if (present.isNegative()) {
                Exception e = new DefaultFrameworkException(String.format(ASSERT_NOT_PRESENT, present.getLocator()));
                onException(result, e);
            }
        } catch (DefaultFrameworkException e) {
            if (!present.isNegative()) {
                onException(result, e);
            }
        }

    }

    private void executeAssertChecked(final AssertChecked checked, final CommandResult result) {
        uiLogUtil.logAssertChecked(checked);
        resultUtil.addAssertCheckedMetadata(checked, result);
        boolean isSelected =
                uiUtil.findWebElement(dependencies, checked.getLocator(), checked.getLocatorStrategy()).isSelected();
        if (checked.isNegative() && isSelected || !checked.isNegative() && !isSelected) {
            Exception e = new DefaultFrameworkException(String
                    .format(ASSERT_CHECKED, checked.getLocator()));
            onException(result, e);
        }
    }

    private void executeComparison(final String actual, final String expected, final CommandResult result,
                                   final boolean isNegative) {
        try {
            new CompareBuilder(dependencies.getFile(), dependencies.getPosition().get(),
                    jacksonService, stringPrettifier).withActual(actual).withExpected(expected).exec();
            if (isNegative) {
                Exception e = new DefaultFrameworkException(String
                        .format(ASSERT_FAILED_EQUAL, expected, actual));
                onException(result, e);
            }
        } catch (Exception e) {
            if (!isNegative) {
                onException(result, e);
            }
        }
    }

    private void onException(final CommandResult result, final Exception e) {
        exceptions.add(e.getMessage());
        logUtil.logException(e);
        resultUtil.setExceptionResult(result, e);
    }

    private void rethrowOnErrors() {
        if (!exceptions.isEmpty()) {
            throw new DefaultFrameworkException(exceptions);
        }
    }

    private interface AssertCmdPredicate extends Predicate<AbstractCommand> {
    }

    private interface AssertMethod extends BiConsumer<AbstractCommand, CommandResult> {
    }
}
