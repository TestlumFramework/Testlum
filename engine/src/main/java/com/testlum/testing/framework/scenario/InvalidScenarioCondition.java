package com.testlum.testing.framework.scenario;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Map;

public class InvalidScenarioCondition implements ExecutionCondition {

    private static final String VALID_SCENARIO = "Valid scenario";

    @Override
    public @NonNull ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
        String displayName = context.getDisplayName();
        for (Map.Entry<String, String> entry : ScenarioStatusRegistry.getInvalid().entrySet()) {
            if (displayName.contains(entry.getKey())) {
                return ConditionEvaluationResult.disabled(entry.getValue());
            }
        }
        return ConditionEvaluationResult.enabled(VALID_SCENARIO);
    }
}
