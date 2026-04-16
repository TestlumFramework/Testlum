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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NativeAssertExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private ConditionUtil conditionUtil;
    @Mock
    private StringPrettifier stringPrettifier;
    @Mock
    private JacksonService jacksonService;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private NativeAssertExecutor executor;
    private ScenarioContext scenarioContext;

    @BeforeEach
    void setUp() {
        scenarioContext = new ScenarioContext(new HashMap<>());
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .position(new AtomicInteger(0))
                .scenarioContext(scenarioContext)
                .file(mock(File.class))
                .build();
        executor = new NativeAssertExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "conditionUtil", conditionUtil);
        ReflectionTestUtils.setField(executor, "stringPrettifier", stringPrettifier);
        ReflectionTestUtils.setField(executor, "jacksonService", jacksonService);
    }

    @Nested
    class Annotation {

        @Test
        void hasExecutorForClassAnnotationForNativeAssert() {
            ExecutorForClass annotation = NativeAssertExecutor.class.getAnnotation(ExecutorForClass.class);
            assertNotNull(annotation);
            assertEquals(NativeAssert.class, annotation.value());
        }
    }

    @Nested
    class Execute {

        @Test
        void handlesEmptyAssertList() {
            NativeAssert nativeAssert = new NativeAssert();
            nativeAssert.getAttributeOrEqualOrNotEqual().addAll(new ArrayList<>());
            CommandResult result = new CommandResult();
            lenient().when(resultUtil.newUiCommandResultInstance(any(Integer.class), any()))
                    .thenReturn(new CommandResult());
            assertDoesNotThrow(() -> executor.execute(nativeAssert, result));
            assertNotNull(result.getSubCommandsResult());
        }

        @Test
        void setsSubCommandsResultList() {
            NativeAssert nativeAssert = new NativeAssert();
            CommandResult result = new CommandResult();

            executor.execute(nativeAssert, result);

            assertNotNull(result.getSubCommandsResult());
            assertTrue(result.getSubCommandsResult().isEmpty());
        }
    }

    @Nested
    class EqualityAssert {

        @Test
        void doesNotThrowWhenAllContentItemsAreEqual() {
            NativeAssert nativeAssert = new NativeAssert();
            AssertEqual assertEqual = new AssertEqual();
            assertEqual.getContent().addAll(List.of("same", "same"));
            nativeAssert.getAttributeOrEqualOrNotEqual().add(assertEqual);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(nativeAssert, result));
        }

        @Test
        void throwsWhenContentItemsAreNotEqual() {
            NativeAssert nativeAssert = new NativeAssert();
            AssertEqual assertEqual = new AssertEqual();
            assertEqual.getContent().addAll(List.of("one", "two"));
            nativeAssert.getAttributeOrEqualOrNotEqual().add(assertEqual);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(nativeAssert, result));
        }
    }

    @Nested
    class InequalityAssert {

        @Test
        void doesNotThrowWhenContentItemsAreDifferent() {
            NativeAssert nativeAssert = new NativeAssert();
            AssertNotEqual assertNotEqual = new AssertNotEqual();
            assertNotEqual.getContent().addAll(List.of("one", "two"));
            nativeAssert.getAttributeOrEqualOrNotEqual().add(assertNotEqual);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(nativeAssert, result));
        }

        @Test
        void throwsWhenContentItemsAreAllEqual() {
            NativeAssert nativeAssert = new NativeAssert();
            AssertNotEqual assertNotEqual = new AssertNotEqual();
            assertNotEqual.getContent().addAll(List.of("same", "same"));
            nativeAssert.getAttributeOrEqualOrNotEqual().add(assertNotEqual);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(nativeAssert, result));
        }
    }

    @Nested
    class AttributeAssert {

        @Test
        void attributeAssertPassesWhenValueMatches() {
            final NativeAssert nativeAssert = new NativeAssert();
            AssertAttribute attr = new AssertAttribute();
            attr.setLocator("inputField");
            attr.setName("value");
            attr.setContent("expected");
            nativeAssert.getAttributeOrEqualOrNotEqual().add(attr);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("inputField"), any()))
                    .thenReturn(mockElement);
            when(mockElement.getAttribute("value")).thenReturn("expected");

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(nativeAssert, result));
        }

        @Test
        void attributeAssertLogsExceptionWhenValueDoesNotMatch() {
            final NativeAssert nativeAssert = new NativeAssert();
            AssertAttribute attr = new AssertAttribute();
            attr.setLocator("inputField");
            attr.setName("value");
            attr.setContent("expected");
            nativeAssert.getAttributeOrEqualOrNotEqual().add(attr);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("inputField"), any()))
                    .thenReturn(mockElement);
            when(mockElement.getAttribute("value")).thenReturn("different");

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            // NativeAssertExecutor does not rethrow; it logs and sets exception on result
            assertDoesNotThrow(() -> executor.execute(nativeAssert, result));
            verify(logUtil).logException(any(Exception.class));
            verify(resultUtil).setExceptionResult(eq(subResult), any(Exception.class));
        }
    }

    @Nested
    class SkippedByCondition {

        @Test
        void skipsSubCommandWhenConditionIsFalse() {
            NativeAssert nativeAssert = new NativeAssert();
            AssertEqual assertEqual = new AssertEqual();
            assertEqual.getContent().addAll(List.of("one", "two"));
            nativeAssert.getAttributeOrEqualOrNotEqual().add(assertEqual);

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(false);

            assertDoesNotThrow(() -> executor.execute(nativeAssert, result));
        }
    }

    @Nested
    class MultipleCommands {

        @Test
        void executesMultipleSubCommandsAndSetsSubResults() {
            NativeAssert nativeAssert = new NativeAssert();

            AssertEqual assertEqual = new AssertEqual();
            assertEqual.getContent().addAll(List.of("same", "same"));
            nativeAssert.getAttributeOrEqualOrNotEqual().add(assertEqual);

            AssertNotEqual assertNotEqual = new AssertNotEqual();
            assertNotEqual.getContent().addAll(List.of("one", "two"));
            nativeAssert.getAttributeOrEqualOrNotEqual().add(assertNotEqual);

            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(subResult))).thenReturn(true);

            assertDoesNotThrow(() -> executor.execute(nativeAssert, result));
            assertNotNull(result.getSubCommandsResult());
            assertEquals(2, result.getSubCommandsResult().size());
        }
    }
}
