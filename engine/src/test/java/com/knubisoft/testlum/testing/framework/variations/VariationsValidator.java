package com.knubisoft.testlum.testing.framework.variations;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.model.scenario.Scenario;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.VARIATIONS_NOT_USED;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.VARIATION_FILE_IS_EMPTY;

public class VariationsValidator {

    private static final String VARIABLE_FORMAT = "{{%s}}";

    public void validateByScenario(final List<Map<String, String>> variationList,
                                   final Scenario scenario,
                                   final File filePath) {
        if (variationList.isEmpty()) {
            throw new DefaultFrameworkException(VARIATION_FILE_IS_EMPTY,
                    scenario.getSettings().getVariations(), filePath.getAbsolutePath());
        }
        String scenarioAsText = JacksonMapperUtil.writeValueAsString(scenario);
        boolean variablesNotUsedInCommands = variationList.get(0).keySet().stream()
                .map(var -> String.format(VARIABLE_FORMAT, var))
                .noneMatch(scenarioAsText::contains);
        if (variablesNotUsedInCommands) {
            throw new DefaultFrameworkException(VARIATIONS_NOT_USED, filePath.getAbsolutePath());
        }
    }
}
