package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.model.scenario.FromConstant;
import com.knubisoft.testlum.testing.model.scenario.NativeVar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NativeVariableExecutorTest {

    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private UiUtil uiUtil;
    @Mock
    private VariableHelper variableHelper;
    @Mock
    private ApplicationContext context;
    @Mock
    private WebDriver driver;

    private NativeVariableExecutor executor;
    private ScenarioContext scenarioContext;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        when(context.getBean(VariableHelper.class)).thenReturn(variableHelper);
        scenarioContext = new ScenarioContext(new HashMap<>());
        ExecutorDependencies deps = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .scenarioContext(scenarioContext)
                .build();
        executor = new NativeVariableExecutor(deps);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
    }

    @Nested
    class Execute {
        @Test
        void setsConstantVariable() {
            NativeVar var = new NativeVar();
            var.setName("myVar");
            FromConstant constant = new FromConstant();
            constant.setValue("hello");
            var.setConstant(constant);

            when(variableHelper.lookupVarMethod(any(), any())).thenAnswer(invocation -> {
                return (VariableHelper.VarMethod<NativeVar>) (v, r) -> "hello";
            });
            doNothing().when(logUtil).logVarInfo(anyString(), anyString());

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(var, result));
            assertEquals("hello", scenarioContext.get("myVar"));
        }
    }
}
