package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionProviderImpl.ConditionUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.AssertAlert;
import com.knubisoft.testlum.testing.model.scenario.AssertAttribute;
import com.knubisoft.testlum.testing.model.scenario.AssertChecked;
import com.knubisoft.testlum.testing.model.scenario.AssertEqual;
import com.knubisoft.testlum.testing.model.scenario.AssertEquality;
import com.knubisoft.testlum.testing.model.scenario.AssertNotEqual;
import com.knubisoft.testlum.testing.model.scenario.AssertPresent;
import com.knubisoft.testlum.testing.model.scenario.AssertTitle;
import com.knubisoft.testlum.testing.model.scenario.WebAssert;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COMMA;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ASSERT_TYPE_NOT_SUPPORTED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONTENT_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CONTENT;

@Slf4j
@ExecutorForClass(WebAssert.class)
public class AssertExecutor extends AbstractUiExecutor<WebAssert> {

    private static final String ASSERT_CONTENT_NOT_EQUAL = "Equality content <%s> is not equal.";
    private static final String ASSERT_CONTENT_IS_EQUAL = "Inequality content <%s> is equal.";
    private static final String ASSERT_NOT_PRESENT = "Element with locator <%s> should not be present.";
    private static final String ASSERT_CHECKED = "Element with locator <%s> failed check assertion.";
    private static final String ASSERT_FAILED_EQUAL = "Property [%s] is equal to [%s]";

    private final List<String> exceptions = new ArrayList<>();
    private final Map<AssertCmdPredicate, AssertMethod> assertCommandMap;

    public AssertExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        assertCommandMap = Map.of(
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
                    ResultUtil.newUiCommandResultInstance(dependencies.getPosition().incrementAndGet(), command);
            subCommandsResult.add(commandResult);
            LogUtil.logAssertCommand(command, dependencies.getPosition().get());
            if (ConditionUtil.isTrue(command.getCondition(), dependencies.getScenarioContext(), commandResult)) {
                executeSubCommand(command, commandResult);
            }
        });
        rethrowOnErrors();
    }

    private void executeSubCommand(final AbstractCommand command, final CommandResult result) {
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
        String actual;
        try {
            actual = getActualValue(attribute, result);
            String expected = attribute.getContent();
            ResultUtil.setExpectedActual(expected, actual, result);
            executeComparison(actual, expected, result, attribute.isNegative());
        } catch (DefaultFrameworkException exception) {
            onException(result, exception);
            UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        }
    }

    private String getActualValue(final AssertAttribute attribute, final CommandResult result) {
        WebElement webElement = UiUtil.findWebElement(dependencies, attribute.getLocator(),
                attribute.getLocatorStrategy(), result);
        return UiUtil.getElementAttribute(webElement, attribute.getName(), dependencies.getDriver());
    }

    private void executeEqualityCommand(final AssertEquality action, final CommandResult result) {
        log.info(CONTENT_LOG, String.join(COMMA, action.getContent()));
        result.put(CONTENT, String.join(COMMA, action.getContent()));
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

    private void executeTitleCommand(final AssertTitle title, final CommandResult result) {
        LogUtil.logAssertTitleCommand(title);
        String actual = dependencies.getDriver().getTitle();
        ResultUtil.setExpectedActual(title.getContent(), actual, result);
        executeComparison(actual, title.getContent(), result, title.isNegative());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void executeAssertAlert(final AssertAlert alert, final CommandResult result) {
        LogUtil.logAssertAlertCommand(alert);
        String actual = dependencies.getDriver().switchTo().alert().getText();
        ResultUtil.setExpectedActual(alert.getText(), actual, result);
        executeComparison(actual, alert.getText(), result, alert.isNegative());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void executeAssertPresent(final AssertPresent present, final CommandResult result) {
        try {
            LogUtil.logAssertPresent(present);
            ResultUtil.addAssertPresentMetadata(present, result);
            UiUtil.findWebElement(dependencies, present.getLocator(), present.getLocatorStrategy(), result);
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
        LogUtil.logAssertChecked(checked);
        ResultUtil.addAssertCheckedMetadata(checked, result);
        boolean isSelected =
                UiUtil.findWebElement(dependencies, checked.getLocator(), checked.getLocatorStrategy(), result).isSelected();
        if ((checked.isNegative() && isSelected) || (!checked.isNegative() && !isSelected)) {
            Exception e = new DefaultFrameworkException(String
                    .format(ASSERT_CHECKED, checked.getLocator()));
            onException(result, e);
        }
    }

    private void executeComparison(final String actual, final String expected, final CommandResult result,
                                   final boolean isNegative) {
        try {
            new CompareBuilder(dependencies.getFile(), dependencies.getPosition().get()).withActual(actual)
                    .withExpected(expected).exec();
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
        LogUtil.logException(e);
        ResultUtil.setExceptionResult(result, e);
    }

    private void rethrowOnErrors() {
        if (!exceptions.isEmpty()) {
            throw new DefaultFrameworkException(exceptions);
        }
    }

    private interface AssertCmdPredicate extends Predicate<AbstractCommand> { }
    private interface AssertMethod extends BiConsumer<AbstractCommand, CommandResult> { }
}
