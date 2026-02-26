package com.knubisoft.testlum.testing.framework.variations;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class GlobalVariationsImpl implements GlobalVariations {

    private final GlobalVariationsProvider globalVariationsProvider;

    @Override
    public List<Map<String, String>> getVariations(final String fileName) {
        return globalVariationsProvider.getVariations(fileName);
    }

    @Override
    public String getValue(final String variation, final Map<String, String> variationMap) {
        return globalVariationsProvider.getValue(variation, variationMap);
    }

    @Override
    public String getValue(final String variation,
                           final Map<String, String> variationMap,
                           final ScenarioContext scenarioContext) {
        return globalVariationsProvider.getValue(variation, variationMap, scenarioContext);
    }


}