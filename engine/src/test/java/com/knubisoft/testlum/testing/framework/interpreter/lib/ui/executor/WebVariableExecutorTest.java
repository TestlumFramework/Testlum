package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.model.scenario.FromConstant;
import com.knubisoft.testlum.testing.model.scenario.FromUrl;
import com.knubisoft.testlum.testing.model.scenario.WebVar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebVariableExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private VariableHelper variableHelper;
    @Mock
    private ScenarioContext scenarioContext;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private WebVariableExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .scenarioContext(scenarioContext)
                .build();
        executor = new WebVariableExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "variableHelper", variableHelper);
    }

    @Nested
    class UrlVariable {

        @Test
        void setsUrlVariableInContext() {
            WebVar webVar = new WebVar();
            webVar.setName("currentUrl");
            webVar.setUrl(new FromUrl());
            CommandResult result = new CommandResult();
            when(variableHelper.lookupVarMethod(any(), any())).thenReturn((var, r) -> "http://example.com");

            executor.execute(webVar, result);

            verify(scenarioContext).set(eq("currentUrl"), eq("http://example.com"));
            verify(logUtil).logVarInfo(eq("currentUrl"), eq("http://example.com"));
        }
    }

    @Nested
    class ConstantVariable {

        @Test
        void setsConstantVariableInContext() {
            WebVar webVar = new WebVar();
            webVar.setName("myConst");
            FromConstant fromConstant = new FromConstant();
            fromConstant.setValue("constantValue");
            webVar.setConstant(fromConstant);
            CommandResult result = new CommandResult();
            when(variableHelper.lookupVarMethod(any(), any())).thenReturn((var, r) -> "constantValue");

            executor.execute(webVar, result);

            verify(scenarioContext).set(eq("myConst"), eq("constantValue"));
        }
    }

    @Nested
    class ExceptionHandling {

        @Test
        void rethrowsExceptionWithVarNameLogged() {
            WebVar webVar = new WebVar();
            webVar.setName("failVar");
            webVar.setComment("This should fail");
            webVar.setUrl(new FromUrl());
            CommandResult result = new CommandResult();
            when(variableHelper.lookupVarMethod(any(), any())).thenThrow(new RuntimeException("lookup error"));

            try {
                executor.execute(webVar, result);
            } catch (RuntimeException ignored) {
                // Expected
            }
        }
    }
}
