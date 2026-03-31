package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.ConditionProvider;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.scenario.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConditionInterpreterTest {

    private ConditionInterpreter interpreter;
    private ConditionProvider conditionProvider;
    private JacksonService jacksonService;
    private ScenarioContext scenarioContext;

    @BeforeEach
    void setUp() {
        ApplicationContext context = mock(ApplicationContext.class);
        conditionProvider = mock(ConditionProvider.class);
        jacksonService = mock(JacksonService.class);
        GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);

        when(context.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(context.getBean(ConditionProvider.class)).thenReturn(conditionProvider);
        when(context.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(context.getBean(JacksonService.class)).thenReturn(jacksonService);
        when(context.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        when(context.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);

        scenarioContext = new ScenarioContext(new HashMap<>());
        InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(context)
                .file(new File("test.xml"))
                .scenarioContext(scenarioContext)
                .position(new AtomicInteger(1))
                .environment("test")
                .build();

        interpreter = new ConditionInterpreter(dependencies);
    }

    @Test
    void processesConditionWithSpelExpression() {
        Condition condition = new Condition();
        condition.setName("isAdmin");
        condition.setSpel("true == true");

        Condition copiedCondition = new Condition();
        copiedCondition.setName("isAdmin");
        copiedCondition.setSpel("true == true");

        when(jacksonService.deepCopy(any(Condition.class), eq(Condition.class))).thenReturn(copiedCondition);
        when(jacksonService.writeValueToCopiedString(any()))
                .thenReturn("{\"name\":\"isAdmin\",\"spel\":\"true == true\"}");
        when(jacksonService.readCopiedValue(anyString(), eq(Condition.class))).thenReturn(copiedCondition);
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

        CommandResult result = new CommandResult();
        result.setId(1);
        interpreter.apply(condition, result);

        verify(conditionProvider).processCondition(eq("isAdmin"), eq("true == true"), eq(scenarioContext), eq(result));
    }

    @Test
    void skipsExecutionWhenConditionApplyIsFalse() {
        Condition condition = new Condition();
        condition.setName("isAdmin");
        condition.setSpel("true == true");
        condition.setCondition("false");

        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(false);

        CommandResult result = new CommandResult();
        result.setId(1);
        interpreter.apply(condition, result);

        verify(conditionProvider, never()).processCondition(any(), any(), any(), any());
    }

    @Test
    void injectsConditionValuesFromScenarioContext() {
        scenarioContext.setCondition("myFlag", true);

        Condition condition = new Condition();
        condition.setName("derived");
        condition.setSpel("myFlag && true");

        Condition copiedCondition = new Condition();
        copiedCondition.setName("derived");
        copiedCondition.setSpel("myFlag && true");

        Condition injectedCondition = new Condition();
        injectedCondition.setName("derived");
        injectedCondition.setSpel("true && true");

        when(jacksonService.deepCopy(any(Condition.class), eq(Condition.class))).thenReturn(copiedCondition);
        when(jacksonService.writeValueToCopiedString(any())).thenReturn("{\"name\":\"derived\"}");
        when(jacksonService.readCopiedValue(anyString(), eq(Condition.class))).thenReturn(injectedCondition);
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

        CommandResult result = new CommandResult();
        result.setId(1);
        interpreter.apply(condition, result);

        verify(conditionProvider).processCondition(eq("derived"), anyString(), eq(scenarioContext), eq(result));
    }
}
