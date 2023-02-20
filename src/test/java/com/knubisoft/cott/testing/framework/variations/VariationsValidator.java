package com.knubisoft.cott.testing.framework.variations;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.cott.testing.model.scenario.Scenario;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.VARIATIONS_NEVER_USED;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.VARIATION_FILE_IS_EMPTY;

public class VariationsValidator {

    public void validateByScenario(final List<Map<String, String>> variationList,
                                   final Scenario scenario,
                                   final File filePath) {
        if (variationList.isEmpty()) {
            throw new DefaultFrameworkException(VARIATION_FILE_IS_EMPTY,
                    scenario.getVariations(), filePath.getAbsolutePath());
        }

        String commands = JacksonMapperUtil.writeValueAsString(scenario.getCommands());
        if (variationList.get(0).keySet().stream().map(var -> "{{" + var + "}}").noneMatch(commands::contains)) {
            throw new DefaultFrameworkException(VARIATIONS_NEVER_USED);
        }
    }
}
