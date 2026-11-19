package com.testlum.testing.framework.configuration.ai;

import com.testlum.testing.framework.EnvToIntegrationMap;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.model.global_config.Ai;
import com.testlum.testing.model.global_config.AiIntegration;
import com.testlum.testing.model.global_config.Integrations;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiConfigurationTest {

    private final AiConfiguration aiConfiguration = new AiConfiguration();

    @Test
    void configuresChatModelWithCustomMaxTokens() throws Exception {
        Ai ai = new Ai();
        ai.setAlias("CUSTOM");
        ai.setEnabled(true);
        ai.setApiKey("test-api-key");
        ai.setBaseUrl("https://api.groq.com/openai/v1");
        ai.setModelName("llama-3.3-70b-versatile");
        ai.setMaxTokens(2048);

        AiIntegration aiIntegration = new AiIntegration();
        aiIntegration.getAi().add(ai);

        Integrations integrations = new Integrations();
        integrations.setAiIntegration(aiIntegration);

        EnvToIntegrationMap envToIntegrationMap = new EnvToIntegrationMap(Map.of("local", integrations));

        Map<AliasEnv, ChatModel> models = aiConfiguration.aiChatModel(envToIntegrationMap);

        assertEquals(1, models.size());
        ChatModel chatModel = models.get(new AliasEnv("CUSTOM", "local"));
        assertNotNull(chatModel);
        assertTrue(chatModel instanceof OpenAiChatModel);

        OpenAiChatModel openAiChatModel = (OpenAiChatModel) chatModel;
        assertEquals(2048, openAiChatModel.defaultRequestParameters().maxCompletionTokens());
    }

    @Test
    void configuresChatModelWithDefaultMaxTokensWhenOmitted() throws Exception {
        Ai ai = new Ai();
        ai.setAlias("DEFAULT");
        ai.setEnabled(true);
        ai.setApiKey("test-api-key");
        ai.setBaseUrl("https://api.groq.com/openai/v1");
        ai.setModelName("llama-3.3-70b-versatile");
        ai.setMaxTokens(null);

        AiIntegration aiIntegration = new AiIntegration();
        aiIntegration.getAi().add(ai);

        Integrations integrations = new Integrations();
        integrations.setAiIntegration(aiIntegration);

        EnvToIntegrationMap envToIntegrationMap = new EnvToIntegrationMap(Map.of("local", integrations));

        Map<AliasEnv, ChatModel> models = aiConfiguration.aiChatModel(envToIntegrationMap);

        assertEquals(1, models.size());
        ChatModel chatModel = models.get(new AliasEnv("DEFAULT", "local"));
        assertNotNull(chatModel);
        assertTrue(chatModel instanceof OpenAiChatModel);

        OpenAiChatModel openAiChatModel = (OpenAiChatModel) chatModel;
        assertEquals(1000, openAiChatModel.defaultRequestParameters().maxCompletionTokens());
    }

    @Test
    void ignoresDisabledAiIntegration() {
        Ai ai = new Ai();
        ai.setAlias("DISABLED");
        ai.setEnabled(false);
        ai.setApiKey("test-api-key");
        ai.setBaseUrl("https://api.groq.com/openai/v1");
        ai.setModelName("llama-3.3-70b-versatile");

        AiIntegration aiIntegration = new AiIntegration();
        aiIntegration.getAi().add(ai);

        Integrations integrations = new Integrations();
        integrations.setAiIntegration(aiIntegration);

        EnvToIntegrationMap envToIntegrationMap = new EnvToIntegrationMap(Map.of("local", integrations));

        Map<AliasEnv, ChatModel> models = aiConfiguration.aiChatModel(envToIntegrationMap);

        assertTrue(models.isEmpty());
    }

}
