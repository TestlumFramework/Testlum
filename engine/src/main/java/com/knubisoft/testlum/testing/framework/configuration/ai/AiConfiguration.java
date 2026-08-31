package com.knubisoft.testlum.testing.framework.configuration.ai;

import com.knubisoft.testlum.testing.framework.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.condition.OnAiEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Ai;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional(OnAiEnabledCondition.class)
public class AiConfiguration {

    private static final int MAX_COMPLETION_TOKENS = 50;

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public Map<AliasEnv, ChatModel> aiChatModel(final EnvToIntegrationMap envTointegrations) {
        Map<AliasEnv, ChatModel> aiChatModelMap = new HashMap<>();
        envTointegrations
                .forEach((env, integrations) -> collectChatModels(integrations, env, aiChatModelMap));
        return aiChatModelMap;
    }

    private void collectChatModels(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, ChatModel> aiChatModelMap) {
        for (Ai ai : integrations.getAiIntegration().getAi()) {
            if (ai.isEnabled()) {
                aiChatModelMap.put(new AliasEnv(ai.getAlias(), env), constructChatModel(ai));
            }
        }
    }

    private ChatModel constructChatModel(final Ai ai) {
        return OpenAiChatModel.builder()
                .apiKey(ai.getApiKey())
                .baseUrl(ai.getBaseUrl())
                .modelName(ai.getModelName())
                .temperature(0.0)
                .maxCompletionTokens(MAX_COMPLETION_TOKENS)
                .build();
    }

}
