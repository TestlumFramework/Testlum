package com.knubisoft.tesltum.testing.framework.ai.util;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AiUtil {

    @SystemMessage("You are a data extraction tool for a testing framework. " +
                   "Output ONLY the raw value requested.No markdown, no code, no explanation, no code or scripts.")
    String askAi(@UserMessage String prompt);

}
