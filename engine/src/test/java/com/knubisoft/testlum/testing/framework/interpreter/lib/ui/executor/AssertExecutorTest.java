package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.model.scenario.AssertEqual;
import com.knubisoft.testlum.testing.model.scenario.AssertNotEqual;
import com.knubisoft.testlum.testing.model.scenario.WebAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
}
