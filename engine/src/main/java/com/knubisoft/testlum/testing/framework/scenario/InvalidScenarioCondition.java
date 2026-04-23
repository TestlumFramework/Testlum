package com.knubisoft.testlum.testing.framework.scenario;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InvalidScenarioCondition implements ExecutionCondition {

    private static final Map<String, String> INVALID_SCENARIOS_ERR_LEVEL = new ConcurrentHashMap<>();
    private static final Map<String, String> INVALID_SCENARIOS_WARN_LEVEL = new ConcurrentHashMap<>();

    public static void registerError(final File scenarioFile, final String reason) {
        String shortenedScenarioPath = shortenScenarioPath(scenarioFile.getPath());
        INVALID_SCENARIOS_ERR_LEVEL.put(shortenedScenarioPath, reason);
    }

    public static void registerWarning(final File scenarioFile, final String reason) {
        String shortenedScenarioPath = shortenScenarioPath(scenarioFile.getPath());
        INVALID_SCENARIOS_WARN_LEVEL.put(shortenedScenarioPath, reason);
    }

    public static Map<String, String> getRegisteredError() {
        return Collections.unmodifiableMap(INVALID_SCENARIOS_ERR_LEVEL);
    }

    public static Map<String, String> getRegisteredWarning() {
        return Collections.unmodifiableMap(INVALID_SCENARIOS_WARN_LEVEL);
    }

    @Override
    public @NonNull ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
        String displayName = context.getDisplayName();
        for (Map.Entry<String, String> entry : INVALID_SCENARIOS_ERR_LEVEL.entrySet()) {
            if (displayName.contains(entry.getKey())) {
                return ConditionEvaluationResult.disabled(entry.getValue());
            }
        }
        return ConditionEvaluationResult.enabled("Valid scenario");
    }

    private static String shortenScenarioPath(final String scenarioPath) {
        return StringUtils.substringAfter(scenarioPath, "scenarios");
    }

}