package com.knubisoft.cott.testing.framework.variations;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.scenario.Scenario;

import java.io.File;
import java.util.List;
import java.util.Map;

public class VariationsValidator {

    public void validateByScenario(final List<Map<String, String>> variationList,
                                   final Scenario scenario,
                                   final File filePath) {
        if (variationList.isEmpty()) {
            throw new DefaultFrameworkException("Variation file %s in the %s scenario is empty",
                    scenario.getVariations(), filePath.getAbsolutePath());
        }

        //todo impl validation
    }
}
