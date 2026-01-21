package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.tesltum.testing.framework.ai.util.AiUtil;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Ai;
import com.knubisoft.testlum.testing.model.scenario.UiAi;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

@Slf4j
@ExecutorForClass(UiAi.class)
public class UiAiExecutor extends AbstractUiExecutor<UiAi> {

    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String NAME_LOG = format(TABLE_FORMAT, "Name", "{}");
    private static final String PROMPT_LOG = format(TABLE_FORMAT, "Prompt", "{}");
    private static final String ANSWER_LOG = format(TABLE_FORMAT, "Answer", "{}");
    public static final String ALIAS = "Alias";
    public static final String NAME = "Name";
    public static final String PROMPT = "Prompt";
    public static final String ANSWER = "Answer";

    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    @Autowired(required = false)
    private Map<AliasEnv, ChatModel> aiChatModels;

    private final Map<AliasEnv, ChatMemory> chatMemoryMap = new ConcurrentHashMap<>();

    public UiAiExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void execute(final UiAi uiAi, final CommandResult result) {
        checkAlias(uiAi);
        logInfoBeforeAnswer(uiAi);
        String answer = executePrompt(uiAi);
        dependencies.getScenarioContext().set(uiAi.getName(), answer);
        addAskAiMetadata(uiAi, answer, result);
        logInfoAfterAnswer(answer);
    }

    private void checkAlias(final UiAi uiAi) {
        if (uiAi.getAlias() == null) {
            uiAi.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private String executePrompt(final UiAi uiAi) {
        AliasEnv aliasEnv = new AliasEnv(uiAi.getAlias(), dependencies.getEnvironment());
        ChatModel chatModel = aiChatModels.get(aliasEnv);
        ChatMemory chatMemory = chatMemoryMap
                .computeIfAbsent(aliasEnv, env -> MessageWindowChatMemory.withMaxMessages(10));

        AiUtil aiService = AiServices.builder(AiUtil.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory)
                .build();

        return aiService.askAi(uiAi.getPrompt()).trim();
    }

    private void logInfoBeforeAnswer(final UiAi uiAi) {
        log.info(ALIAS_LOG, uiAi.getAlias());
        log.info(NAME_LOG, uiAi.getName());
        log.info(PROMPT_LOG, uiAi.getPrompt());
    }

    private void addAskAiMetadata(final UiAi uiAi, final String answer, final CommandResult result) {
        result.put(ALIAS, uiAi.getAlias());
        result.put(NAME, uiAi.getName());
        result.put(PROMPT, uiAi.getPrompt());
        result.put(ANSWER, answer);
    }

    public void logInfoAfterAnswer(final String answer) {
        log.info(ANSWER_LOG, answer);
    }

}
