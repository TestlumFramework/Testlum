package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.ConditionUtil;
import com.knubisoft.testlum.testing.model.scenario.UiCondition;
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
class UiConditionExecutorTest {

    @Mock
    private ConditionUtil conditionUtil;
    @Mock
    private ScenarioContext scenarioContext;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private UiConditionExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .scenarioContext(scenarioContext)
                .build();
        executor = new UiConditionExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "conditionUtil", conditionUtil);
    }

    @Nested
    class Execute {

        @Test
        void delegatesToConditionUtilWithNameAndSpel() {
            UiCondition condition = new UiCondition();
            condition.setName("isLoggedIn");
            condition.setSpel("#user != null");
            CommandResult result = new CommandResult();

            executor.execute(condition, result);

            verify(conditionUtil).processCondition(
                    eq("isLoggedIn"), eq("#user != null"), eq(scenarioContext), eq(result));
        }
    }
}
