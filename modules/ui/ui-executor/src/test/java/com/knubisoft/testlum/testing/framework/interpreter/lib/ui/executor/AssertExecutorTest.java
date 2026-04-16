package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.model.scenario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssertExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private ConditionUtil conditionUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private StringPrettifier stringPrettifier;
    @Mock
    private JacksonService jacksonService;
    @Mock
    private WebDriver driver;
    @Mock
    private ScenarioContext scenarioContext;
    @Mock
    private ApplicationContext context;

    private AssertExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .scenarioContext(scenarioContext)
                .position(new AtomicInteger(0))
                .file(mock(File.class))
                .build();
        executor = new AssertExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "conditionUtil", conditionUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "stringPrettifier", stringPrettifier);
        ReflectionTestUtils.setField(executor, "jacksonService", jacksonService);
        lenient().when(stringPrettifier.prettify(anyString())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    class Annotation {

        @Test
        void hasExecutorForClassAnnotationForWebAssert() {
            ExecutorForClass annotation = AssertExecutor.class.getAnnotation(ExecutorForClass.class);
            assertNotNull(annotation);
            assertEquals(WebAssert.class, annotation.value());
        }
    }

    @Nested
    class CommandMapSetup {

        @Test
        void assertCommandMapIsInitializedWithSixEntries() {
            Object commandMap = ReflectionTestUtils.getField(executor, "assertCommandMap");
            assertNotNull(commandMap);
            assertTrue(commandMap instanceof java.util.Map);
            assertEquals(6, ((java.util.Map<?, ?>) commandMap).size());
        }
    }

    @Nested
    class EqualityAssert {

        @Test
        void doesNotThrowWhenAllContentItemsAreEqual() {
            WebAssert webAssert = new WebAssert();
            AssertEqual assertEqual = new AssertEqual();
            assertEqual.getContent().addAll(List.of("same", "same"));
            webAssert.getAttributeOrTitleOrEqual().add(assertEqual);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }

        @Test
        void throwsWhenContentItemsAreNotEqual() {
            WebAssert webAssert = new WebAssert();
            AssertEqual assertEqual = new AssertEqual();
            assertEqual.getContent().addAll(List.of("one", "two"));
            webAssert.getAttributeOrTitleOrEqual().add(assertEqual);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(webAssert, result));
        }

        @Test
        void doesNotThrowWhenEqualWithThreeIdenticalItems() {
            WebAssert webAssert = new WebAssert();
            AssertEqual assertEqual = new AssertEqual();
            assertEqual.getContent().addAll(List.of("x", "x", "x"));
            webAssert.getAttributeOrTitleOrEqual().add(assertEqual);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }
    }

    @Nested
    class InequalityAssert {

        @Test
        void doesNotThrowWhenContentItemsAreDifferent() {
            WebAssert webAssert = new WebAssert();
            AssertNotEqual assertNotEqual = new AssertNotEqual();
            assertNotEqual.getContent().addAll(List.of("one", "two"));
            webAssert.getAttributeOrTitleOrEqual().add(assertNotEqual);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }

        @Test
        void throwsWhenContentItemsAreAllEqual() {
            WebAssert webAssert = new WebAssert();
            AssertNotEqual assertNotEqual = new AssertNotEqual();
            assertNotEqual.getContent().addAll(List.of("same", "same"));
            webAssert.getAttributeOrTitleOrEqual().add(assertNotEqual);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(webAssert, result));
        }
    }

    @Nested
    class TitleAssert {

        @Test
        void titleAssertPassesWhenTitleMatches() {
            WebAssert webAssert = new WebAssert();
            AssertTitle title = new AssertTitle();
            title.setContent("My Page");
            title.setNegative(false);
            webAssert.getAttributeOrTitleOrEqual().add(title);

            when(driver.getTitle()).thenReturn("My Page");
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }

        @Test
        void titleAssertCollectsErrorWhenTitleDoesNotMatch() {
            WebAssert webAssert = new WebAssert();
            AssertTitle title = new AssertTitle();
            title.setContent("Expected Title");
            title.setNegative(false);
            webAssert.getAttributeOrTitleOrEqual().add(title);

            when(driver.getTitle()).thenReturn("Actual Title");
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(webAssert, result));
        }

        @Test
        void negativeTitleAssertPassesWhenTitleDoesNotMatch() {
            WebAssert webAssert = new WebAssert();
            AssertTitle title = new AssertTitle();
            title.setContent("Expected Title");
            title.setNegative(true);
            webAssert.getAttributeOrTitleOrEqual().add(title);

            when(driver.getTitle()).thenReturn("Different Title");
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }

        @Test
        void negativeTitleAssertCollectsErrorWhenTitleMatches() {
            WebAssert webAssert = new WebAssert();
            AssertTitle title = new AssertTitle();
            title.setContent("My Page");
            title.setNegative(true);
            webAssert.getAttributeOrTitleOrEqual().add(title);

            when(driver.getTitle()).thenReturn("My Page");
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(webAssert, result));
        }
    }

    @Nested
    class AlertAssert {

        @Test
        void alertAssertPassesWhenAlertTextMatches() {
            WebAssert webAssert = new WebAssert();
            AssertAlert alert = new AssertAlert();
            alert.setText("Alert message");
            alert.setNegative(false);
            webAssert.getAttributeOrTitleOrEqual().add(alert);

            WebDriver.TargetLocator targetLocator = mock(WebDriver.TargetLocator.class);
            org.openqa.selenium.Alert seleniumAlert = mock(org.openqa.selenium.Alert.class);
            when(driver.switchTo()).thenReturn(targetLocator);
            when(targetLocator.alert()).thenReturn(seleniumAlert);
            when(seleniumAlert.getText()).thenReturn("Alert message");

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }

        @Test
        void alertAssertCollectsErrorWhenAlertTextDoesNotMatch() {
            WebAssert webAssert = new WebAssert();
            AssertAlert alert = new AssertAlert();
            alert.setText("Expected alert");
            alert.setNegative(false);
            webAssert.getAttributeOrTitleOrEqual().add(alert);

            WebDriver.TargetLocator targetLocator = mock(WebDriver.TargetLocator.class);
            org.openqa.selenium.Alert seleniumAlert = mock(org.openqa.selenium.Alert.class);
            when(driver.switchTo()).thenReturn(targetLocator);
            when(targetLocator.alert()).thenReturn(seleniumAlert);
            when(seleniumAlert.getText()).thenReturn("Actual alert");

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(webAssert, result));
        }

        @Test
        void negativeAlertAssertPassesWhenAlertTextDiffers() {
            WebAssert webAssert = new WebAssert();
            AssertAlert alert = new AssertAlert();
            alert.setText("Expected alert");
            alert.setNegative(true);
            webAssert.getAttributeOrTitleOrEqual().add(alert);

            WebDriver.TargetLocator targetLocator = mock(WebDriver.TargetLocator.class);
            org.openqa.selenium.Alert seleniumAlert = mock(org.openqa.selenium.Alert.class);
            when(driver.switchTo()).thenReturn(targetLocator);
            when(targetLocator.alert()).thenReturn(seleniumAlert);
            when(seleniumAlert.getText()).thenReturn("Different alert");

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }
    }

    @Nested
    class PresentAssert {

        @Test
        void assertPresentPassesWhenElementIsFound() {
            WebAssert webAssert = new WebAssert();
            AssertPresent present = new AssertPresent();
            present.setLocator("myElement");
            present.setNegative(false);
            webAssert.getAttributeOrTitleOrEqual().add(present);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("myElement"), any()))
                    .thenReturn(mockElement);

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }

        @Test
        void assertPresentCollectsErrorWhenElementNotFoundAndPositive() {
            WebAssert webAssert = new WebAssert();
            AssertPresent present = new AssertPresent();
            present.setLocator("missingElement");
            present.setNegative(false);
            webAssert.getAttributeOrTitleOrEqual().add(present);

            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("missingElement"), any()))
                    .thenThrow(new DefaultFrameworkException("Element not found"));

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(webAssert, result));
        }

        @Test
        void negativeAssertPresentPassesWhenElementNotFound() {
            WebAssert webAssert = new WebAssert();
            AssertPresent present = new AssertPresent();
            present.setLocator("missingElement");
            present.setNegative(true);
            webAssert.getAttributeOrTitleOrEqual().add(present);

            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("missingElement"), any()))
                    .thenThrow(new DefaultFrameworkException("Element not found"));

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }

        @Test
        void negativeAssertPresentCollectsErrorWhenElementIsFound() {
            WebAssert webAssert = new WebAssert();
            AssertPresent present = new AssertPresent();
            present.setLocator("existingElement");
            present.setNegative(true);
            webAssert.getAttributeOrTitleOrEqual().add(present);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("existingElement"), any()))
                    .thenReturn(mockElement);

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(webAssert, result));
        }
    }

    @Nested
    class CheckedAssert {

        @Test
        void assertCheckedPassesWhenElementIsSelected() {
            WebAssert webAssert = new WebAssert();
            AssertChecked checked = new AssertChecked();
            checked.setLocator("checkbox");
            checked.setNegative(false);
            webAssert.getAttributeOrTitleOrEqual().add(checked);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("checkbox"), any()))
                    .thenReturn(mockElement);
            when(mockElement.isSelected()).thenReturn(true);

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }

        @Test
        void assertCheckedCollectsErrorWhenElementIsNotSelected() {
            WebAssert webAssert = new WebAssert();
            AssertChecked checked = new AssertChecked();
            checked.setLocator("checkbox");
            checked.setNegative(false);
            webAssert.getAttributeOrTitleOrEqual().add(checked);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("checkbox"), any()))
                    .thenReturn(mockElement);
            when(mockElement.isSelected()).thenReturn(false);

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(webAssert, result));
        }

        @Test
        void negativeAssertCheckedPassesWhenElementIsNotSelected() {
            WebAssert webAssert = new WebAssert();
            AssertChecked checked = new AssertChecked();
            checked.setLocator("checkbox");
            checked.setNegative(true);
            webAssert.getAttributeOrTitleOrEqual().add(checked);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("checkbox"), any()))
                    .thenReturn(mockElement);
            when(mockElement.isSelected()).thenReturn(false);

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }

        @Test
        void negativeAssertCheckedCollectsErrorWhenElementIsSelected() {
            WebAssert webAssert = new WebAssert();
            AssertChecked checked = new AssertChecked();
            checked.setLocator("checkbox");
            checked.setNegative(true);
            webAssert.getAttributeOrTitleOrEqual().add(checked);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("checkbox"), any()))
                    .thenReturn(mockElement);
            when(mockElement.isSelected()).thenReturn(true);

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(webAssert, result));
        }
    }

    @Nested
    class AttributeAssert {

        @Test
        void attributeAssertPassesWhenValueMatches() {
            final WebAssert webAssert = new WebAssert();
            AssertAttribute attr = new AssertAttribute();
            attr.setLocator("inputField");
            attr.setName("value");
            attr.setContent("expected");
            attr.setNegative(false);
            webAssert.getAttributeOrTitleOrEqual().add(attr);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("inputField"), any()))
                    .thenReturn(mockElement);
            when(uiUtil.getElementAttribute(eq(mockElement), eq("value"), eq(driver)))
                    .thenReturn("expected");

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }

        @Test
        void attributeAssertCollectsErrorWhenValueDoesNotMatch() {
            final WebAssert webAssert = new WebAssert();
            AssertAttribute attr = new AssertAttribute();
            attr.setLocator("inputField");
            attr.setName("value");
            attr.setContent("expected");
            attr.setNegative(false);
            webAssert.getAttributeOrTitleOrEqual().add(attr);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("inputField"), any()))
                    .thenReturn(mockElement);
            when(uiUtil.getElementAttribute(eq(mockElement), eq("value"), eq(driver)))
                    .thenReturn("actual-different");

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(webAssert, result));
        }

        @Test
        void negativeAttributeAssertPassesWhenValueDoesNotMatch() {
            final WebAssert webAssert = new WebAssert();
            AssertAttribute attr = new AssertAttribute();
            attr.setLocator("inputField");
            attr.setName("value");
            attr.setContent("expected");
            attr.setNegative(true);
            webAssert.getAttributeOrTitleOrEqual().add(attr);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("inputField"), any()))
                    .thenReturn(mockElement);
            when(uiUtil.getElementAttribute(eq(mockElement), eq("value"), eq(driver)))
                    .thenReturn("different");

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }

        @Test
        void negativeAttributeAssertCollectsErrorWhenValueMatches() {
            final WebAssert webAssert = new WebAssert();
            AssertAttribute attr = new AssertAttribute();
            attr.setLocator("inputField");
            attr.setName("value");
            attr.setContent("same");
            attr.setNegative(true);
            webAssert.getAttributeOrTitleOrEqual().add(attr);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("inputField"), any()))
                    .thenReturn(mockElement);
            when(uiUtil.getElementAttribute(eq(mockElement), eq("value"), eq(driver)))
                    .thenReturn("same");

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(webAssert, result));
        }
    }

    @Nested
    class SkippedByCondition {

        @Test
        void skipsSubCommandWhenConditionIsFalse() {
            WebAssert webAssert = new WebAssert();
            AssertEqual assertEqual = new AssertEqual();
            assertEqual.getContent().addAll(List.of("one", "two"));
            webAssert.getAttributeOrTitleOrEqual().add(assertEqual);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(false);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
        }
    }

    @Nested
    class UnsupportedCommand {

        @Test
        void throwsForUnsupportedAssertCommandType() {
            WebAssert webAssert = new WebAssert();
            // Use a generic AbstractCommand subclass that is not in the command map
            AbstractCommand unsupported = new AbstractCommand() { };
            webAssert.getAttributeOrTitleOrEqual().add(unsupported);

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(webAssert, result));
        }
    }

    @Nested
    class MultipleCommands {

        @Test
        void executesMultipleSubCommands() {
            WebAssert webAssert = new WebAssert();

            AssertEqual assertEqual = new AssertEqual();
            assertEqual.getContent().addAll(List.of("same", "same"));
            webAssert.getAttributeOrTitleOrEqual().add(assertEqual);

            AssertNotEqual assertNotEqual = new AssertNotEqual();
            assertNotEqual.getContent().addAll(List.of("one", "two"));
            webAssert.getAttributeOrTitleOrEqual().add(assertNotEqual);

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(webAssert, result));
            assertNotNull(result.getSubCommandsResult());
            assertEquals(2, result.getSubCommandsResult().size());
        }

        @Test
        void setsSubCommandsResultList() {
            WebAssert webAssert = new WebAssert();
            CommandResult result = new CommandResult();

            executor.execute(webAssert, result);

            assertNotNull(result.getSubCommandsResult());
            assertTrue(result.getSubCommandsResult().isEmpty());
        }
    }
}
