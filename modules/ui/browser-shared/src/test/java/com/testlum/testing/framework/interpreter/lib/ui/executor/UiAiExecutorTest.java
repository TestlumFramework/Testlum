package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionUtil;
import com.testlum.testing.framework.util.ConfigUtil;
import com.testlum.testing.framework.util.ImageComparisonUtil;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.JavascriptUtil;
import com.testlum.testing.framework.util.LogUtil;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.ScenarioInjectionUtil;
import com.testlum.testing.framework.util.ScreenshotUtil;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.framework.util.UiLogUtil;
import com.testlum.testing.framework.util.UiUtil;
import com.testlum.testing.framework.util.WebDownloadUtil;
import com.testlum.testing.model.scenario.UiAi;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UiAiExecutorTest {

    private static final String ENV = "test";

    private UiAiExecutor executor;
    private ScenarioContext scenarioContext;
    private ChatModel chatModel;

    @BeforeEach
    void setUp() throws Exception {
        chatModel = mock(ChatModel.class);
        ApplicationContext context = mockContext();
        scenarioContext = new ScenarioContext(new HashMap<>());
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .file(new java.io.File("test.xml"))
                .position(new java.util.concurrent.atomic.AtomicInteger(1))
                .scenarioContext(scenarioContext)
                .environment(ENV)
                .build();
        executor = new UiAiExecutor(dependencies);
        setAiChatModels(Map.of(new AliasEnv("DEFAULT", ENV), chatModel));
    }

    private ApplicationContext mockContext() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        mockBeans(ctx);
        return ctx;
    }

    private void mockBeans(final ApplicationContext ctx) {
        mockContextUtils(ctx);
        mockContextSupport(ctx);
    }

    private void mockContextUtils(final ApplicationContext ctx) {
        ConditionUtil conditionUtil = mock(ConditionUtil.class);
        when(conditionUtil.isTrue(any(), any(), any())).thenReturn(true);
        when(ctx.getBean(ConditionUtil.class)).thenReturn(conditionUtil);
        when(ctx.getBean(JacksonService.class)).thenReturn(new JacksonService());
        ScenarioInjectionUtil injectionUtil = mock(ScenarioInjectionUtil.class);
        when(injectionUtil.injectObject(any(), any())).thenAnswer(inv -> inv.getArgument(0));
        when(ctx.getBean(ScenarioInjectionUtil.class)).thenReturn(injectionUtil);
        when(ctx.getBean(UiLogUtil.class)).thenReturn(mock(UiLogUtil.class));
    }

    private void mockContextSupport(final ApplicationContext ctx) {
        when(ctx.getBean(UiUtil.class)).thenReturn(mock(UiUtil.class));
        when(ctx.getBean(ResultUtil.class)).thenReturn(mock(ResultUtil.class));
        when(ctx.getBean(JavascriptUtil.class)).thenReturn(mock(JavascriptUtil.class));
        when(ctx.getBean(ImageComparisonUtil.class)).thenReturn(mock(ImageComparisonUtil.class));
        when(ctx.getBean(ConfigUtil.class)).thenReturn(mock(ConfigUtil.class));
        when(ctx.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(ctx.getBean(LogUtil.class)).thenReturn(mock(LogUtil.class));
        when(ctx.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        when(ctx.getBean(WebDownloadUtil.class)).thenReturn(mock(WebDownloadUtil.class));
        when(ctx.getBean(ScreenshotUtil.class)).thenReturn(mock(ScreenshotUtil.class));
    }

    private void setAiChatModels(final Map<AliasEnv, ChatModel> models) throws Exception {
        Field field = UiAiExecutor.class.getDeclaredField("aiChatModels");
        field.setAccessible(true);
        field.set(executor, models);
    }

    private UiAi createUiAi(final String name, final String prompt) {
        UiAi uiAi = new UiAi();
        uiAi.setName(name);
        uiAi.setPrompt(prompt);
        return uiAi;
    }

    @Test
    void executesPromptSuccessfullyAndSetsScenarioContext() {
        when(chatModel.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class)))
                .thenReturn(ChatResponse.builder().aiMessage(AiMessage.from("195")).build());
        UiAi uiAi = createUiAi("countryCount", "How many countries?");
        CommandResult result = new CommandResult();

        executor.apply(uiAi, result);

        assertEquals("195", scenarioContext.get("countryCount"));
        assertEquals("195", result.getMetadata().get("Answer"));
    }

    @Test
    void throwsExceptionWhenChatModelNotConfigured() throws Exception {
        setAiChatModels(new HashMap<>());
        UiAi uiAi = createUiAi("testVar", "Hello");
        CommandResult result = new CommandResult();

        DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> executor.apply(uiAi, result));
        assertTrue(ex.getMessage().contains("AI chat model not configured for alias 'DEFAULT'"));
    }

    @Test
    void throwsExceptionWhenModelReturnsNullAnswer() {
        ChatResponse chatResponse = ChatResponse.builder()
                .aiMessage(AiMessage.builder().text(null).build())
                .build();
        when(chatModel.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class)))
                .thenReturn(chatResponse);
        UiAi uiAi = createUiAi("testVar", "Hello");
        CommandResult result = new CommandResult();

        DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> executor.apply(uiAi, result));
        assertTrue(ex.getMessage().contains("AI model returned an empty response for alias 'DEFAULT'"));
    }

    @Test
    void throwsExceptionWhenModelReturnsBlankAnswer() {
        when(chatModel.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class)))
                .thenReturn(ChatResponse.builder().aiMessage(AiMessage.from("   ")).build());
        UiAi uiAi = createUiAi("testVar", "Hello");
        CommandResult result = new CommandResult();

        DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> executor.apply(uiAi, result));
        assertTrue(ex.getMessage().contains("AI model returned an empty response for alias 'DEFAULT'"));
    }
}
