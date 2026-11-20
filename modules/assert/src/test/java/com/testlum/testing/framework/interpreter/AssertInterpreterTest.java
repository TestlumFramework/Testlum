package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Assert;
import com.testlum.testing.model.scenario.AssertJsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** The jsonPath assertion as the engine runs it: which body it reads and how a failure is recorded. */
class AssertInterpreterTest {

    private static final String LAST_RESPONSE = "{\"id\":1,\"name\":\"last\"}";
    private static final String SAVED = "{\"id\":2,\"name\":\"saved\"}";

    @TempDir
    File tempDir;

    private AssertInterpreter interpreter;

    @BeforeEach
    void setUp() {
        ApplicationContext context = mock(ApplicationContext.class);
        ConditionProvider conditions = mock(ConditionProvider.class);
        when(conditions.isTrue(any(), any(), any())).thenReturn(true);
        when(context.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(context.getBean(ConditionProvider.class)).thenReturn(conditions);
        when(context.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(context.getBean(JacksonService.class)).thenReturn(new JacksonService());
        when(context.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        when(context.getBean(GlobalTestConfiguration.class)).thenReturn(mock(GlobalTestConfiguration.class));
        Map<String, String> saved = new LinkedHashMap<>();
        saved.put("kept", SAVED);
        saved.put("body", LAST_RESPONSE);
        interpreter = new AssertInterpreter(InterpreterDependencies.builder().context(context)
                .file(new File(tempDir, "scenario.xml")).scenarioContext(new ScenarioContext(saved))
                .position(new AtomicInteger(0)).environment("test").build());
    }

    private static Assert jsonPath(final String fromVar, final String equals) {
        AssertJsonPath check = new AssertJsonPath();
        check.setPath("$.name");
        check.setEquals(equals);
        check.setFromVar(fromVar);
        Assert assertion = new Assert();
        assertion.getEqualOrNotEqualOrJsonPath().add(check);
        return assertion;
    }

    private CommandResult run(final Assert assertion) {
        CommandResult result = new CommandResult();
        interpreter.apply(assertion, result);
        return result;
    }

    @Test
    void readsTheLastResponseWhenNoVariableIsNamed() {
        assertNull(run(jsonPath(null, "last")).getSubCommandsResult().get(0).getException());
    }

    @Test
    void readsTheNamedVariableInsteadOfTheLastResponse() {
        assertNull(run(jsonPath("kept", "saved")).getSubCommandsResult().get(0).getException());
    }

    @Test
    void aMismatchIsRecordedAsAFailureOfThatCommand() {
        CommandResult result = run(jsonPath("kept", "last"));
        String failure = String.valueOf(result.getSubCommandsResult().get(0).getException());
        assertTrue(failure.contains("expected [last] but was [saved]"), failure);
        assertEquals(1, result.getSubCommandsResult().size());
    }
}
