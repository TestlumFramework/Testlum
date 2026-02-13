package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.tesltum.testing.framework.ai.util.AiUtil;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Ai;
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
@InterpreterForClass(Ai.class)
public class AiInterpreter extends AbstractInterpreter<Ai> {

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
    protected void acceptImpl(final Ai o, final CommandResult result) {
        Ai ai = injectCommand(o);
        checkAlias(ai);
        logInfoBeforeAnswer(ai);
        String answer = executePrompt(ai);
        dependencies.getScenarioContext().set(ai.getName(), answer);
        addAskAiMetadata(ai, answer, result);
        logInfoAfterAnswer(answer);
    }

    private void checkAlias(final Ai ai) {
        if (ai.getAlias() == null) {
            ai.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private String executePrompt(final Ai ai) {
        AliasEnv aliasEnv = new AliasEnv(ai.getAlias(), dependencies.getEnvironment());
        ChatModel chatModel = aiChatModels.get(aliasEnv);
        ChatMemory chatMemory = chatMemoryMap
                .computeIfAbsent(aliasEnv, env -> MessageWindowChatMemory.withMaxMessages(10));

        AiUtil aiService = AiServices.builder(AiUtil.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory)
                .build();

        return aiService.askAi(ai.getPrompt()).trim();
    }

    private void logInfoBeforeAnswer(final Ai ai) {
        log.info(ALIAS_LOG, ai.getAlias());
        log.info(NAME_LOG, ai.getName());
        log.info(PROMPT_LOG, ai.getPrompt());
    }

    private void addAskAiMetadata(final Ai ai, final String answer, final CommandResult result) {
        result.put(ALIAS, ai.getAlias());
        result.put(NAME, ai.getName());
        result.put(PROMPT, ai.getPrompt());
        result.put(ANSWER, answer);
    }

    public void logInfoAfterAnswer(final String answer) {
        log.info(ANSWER_LOG, answer);
    }

}
