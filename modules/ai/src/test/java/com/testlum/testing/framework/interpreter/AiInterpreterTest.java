package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Ai;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiInterpreterTest {

    private static final String ENV = "test";

    private AiInterpreter interpreter;
    private ScenarioContext scenarioContext;
    private ChatModel chatModel;

    @BeforeEach
    void setUp() throws Exception {
        chatModel = mock(ChatModel.class);

        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        ConditionProvider conditionProvider = mock(ConditionProvider.class);
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);
        when(context.getBean(ConditionProvider.class)).thenReturn(conditionProvider);
        when(context.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(context.getBean(JacksonService.class)).thenReturn(new JacksonService());
        when(context.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);
        when(context.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);

        scenarioContext = new ScenarioContext(new HashMap<>());
        InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(context)
                .file(new File("test.xml"))
                .scenarioContext(scenarioContext)
                .position(new AtomicInteger(1))
                .environment(ENV)
                .build();

        interpreter = new AiInterpreter(dependencies);

        Map<AliasEnv, ChatModel> aiChatModels = new HashMap<>();
        aiChatModels.put(new AliasEnv("DEFAULT", ENV), chatModel);
        setField(interpreter, "aiChatModels", aiChatModels);
    }

    @Test
    void executeSuccessfully() {
        ChatResponse chatResponse = ChatResponse.builder()
                .aiMessage(AiMessage.from("195"))
                .build();
        when(chatModel.chat(any(ChatRequest.class))).thenReturn(chatResponse);

        Ai ai = new Ai();
        ai.setName("countries");
        ai.setPrompt("How many countries are there?");
        CommandResult result = new CommandResult();

        interpreter.apply(ai, result);

        assertEquals("195", scenarioContext.get("countries"));
        assertEquals("195", result.getMetadata().get("Answer"));
        assertEquals("DEFAULT", result.getMetadata().get("Alias"));
        assertEquals("countries", result.getMetadata().get("Name"));
        assertEquals("How many countries are there?", result.getMetadata().get("Prompt"));
    }

    @Test
    void throwsExceptionWhenModelNotConfigured() {
        Ai ai = new Ai();
        ai.setAlias("NON_EXISTING");
        ai.setName("result");
        ai.setPrompt("Hello");
        CommandResult result = new CommandResult();

        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> interpreter.apply(ai, result));

        assertTrue(exception.getMessage().contains("AI chat model not configured for alias 'NON_EXISTING'"));
    }

    @Test
    void throwsExceptionWhenResponseEmpty() {
        ChatResponse chatResponse = ChatResponse.builder()
                .aiMessage(AiMessage.builder().text(null).build())
                .build();
        when(chatModel.chat(any(ChatRequest.class))).thenReturn(chatResponse);

        Ai ai = new Ai();
        ai.setName("countries");
        ai.setPrompt("How many countries?");
        CommandResult result = new CommandResult();

        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> interpreter.apply(ai, result));

        assertTrue(exception.getMessage().contains("AI model returned an empty response for alias 'DEFAULT'"));
    }

    @Test
    void throwsExceptionWhenResponseBlank() {
        ChatResponse chatResponse = ChatResponse.builder()
                .aiMessage(AiMessage.from("   "))
                .build();
        when(chatModel.chat(any(ChatRequest.class))).thenReturn(chatResponse);

        Ai ai = new Ai();
        ai.setName("countries");
        ai.setPrompt("How many countries?");
        CommandResult result = new CommandResult();

        DefaultFrameworkException exception = assertThrows(DefaultFrameworkException.class,
                () -> interpreter.apply(ai, result));

        assertTrue(exception.getMessage().contains("AI model returned an empty response for alias 'DEFAULT'"));
    }

    private static void setField(final Object target, final String fieldName, final Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

}
