package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.tesltum.testing.framework.ai.util.AiUtil;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.AskAi;
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
@InterpreterForClass(AskAi.class)
public class AiInterpreter extends AbstractInterpreter<AskAi> {

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

    private final Map<AliasEnv, ChatMemory> chatMemoryMap = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private Map<AliasEnv, ChatModel> aiChatModels;

    public AiInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final AskAi o, final CommandResult result) {
        AskAi askAi = injectCommand(o);
        checkAlias(askAi);
        logInfoBeforeAnswer(askAi);
        String answer = executePrompt(askAi);
        dependencies.getScenarioContext().set(askAi.getName(), answer);
        addAskAiMetadata(askAi, answer, result);
        logInfoAfterAnswer(answer);
    }

    private void checkAlias(final AskAi askAi) {
        if (askAi.getAlias() == null) {
            askAi.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private String executePrompt(final AskAi askAi) {
        AliasEnv aliasEnv = new AliasEnv(askAi.getAlias(), dependencies.getEnvironment());
        ChatModel chatModel = aiChatModels.get(aliasEnv);
        ChatMemory chatMemory = chatMemoryMap
                .computeIfAbsent(aliasEnv, env -> MessageWindowChatMemory.withMaxMessages(10));

        AiUtil aiService = AiServices.builder(AiUtil.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory)
                .build();

        return aiService.askAi(askAi.getPrompt()).trim();
    }

    private void logInfoBeforeAnswer(final AskAi askAi) {
        log.info(ALIAS_LOG, askAi.getAlias());
        log.info(NAME_LOG, askAi.getName());
        log.info(PROMPT_LOG, askAi.getPrompt());
    }

    private void addAskAiMetadata(final AskAi askAi, final String answer, final CommandResult result) {
        result.put(ALIAS, askAi.getAlias());
        result.put(NAME, askAi.getName());
        result.put(PROMPT, askAi.getPrompt());
        result.put(ANSWER, answer);
    }

    public void logInfoAfterAnswer(final String answer) {
        log.info(ANSWER_LOG, answer);
    }

}
