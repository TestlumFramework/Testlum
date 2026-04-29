package com.knubisoft.testlum.testing.framework.variations;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.parser.CSVParser;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
public class GlobalVariationsProvider {

    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final String NO_VALUE_FOUND_FOR_KEY = "Unable to find value for key <%s>. Available keys: %s";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);

    private static final Map<String, List<Map<String, String>>> VARIATIONS = new ConcurrentHashMap<>();

    private final VariationsValidator variationsValidator;
    private final CSVParser csvParser;

    public void process(final Scenario scenario, final File filePath) {
        String fileName = scenario.getSettings().getVariations();
        List<Map<String, String>> variationList =
                VARIATIONS.computeIfAbsent(fileName, k -> csvParser.parseVariations(k));
        variationsValidator.validateByScenario(variationList, scenario, filePath);
    }

    public void process(final String variationsFileName) {
        if (StringUtils.isNotBlank(variationsFileName)) {
            VARIATIONS.computeIfAbsent(variationsFileName, k -> csvParser.parseVariations(k));
        }
    }

    public List<Map<String, String>> getVariations(final String fileName) {
        List<Map<String, String>> variationList = VARIATIONS.get(fileName);
        if (Objects.isNull(variationList)) {
            throw new DefaultFrameworkException(ExceptionMessage.VARIATIONS_NOT_FOUND, fileName);
        }
        return variationList;
    }

    public String getValue(final String variation, final Map<String, String> variationMap) {
        if (StringUtils.isBlank(variation)) {
            return variation;
        }
        Matcher m = ROUTE_PATTERN.matcher(variation);
        return getVariationFromMap(variation, m, variationMap, null);
    }

    public String getValue(final String variation,
                           final Map<String, String> variationMap,
                           final ScenarioContext scenarioContext) {
        if (StringUtils.isBlank(variation)) {
            return variation;
        }
        Matcher m = ROUTE_PATTERN.matcher(variation);
        return getVariationFromMap(variation, m, variationMap, scenarioContext);
    }

    private String getVariationFromMap(final String variation,
                                       final Matcher m,
                                       final Map<String, String> variationMap,
                                       final ScenarioContext scenarioContext) {
        String finalValue = variation;
        while (m.find()) {
            String variationKey = m.group(1);
            String variationKeyInBraces = m.group(0);
            String variationValue = variationMap.get(variationKey);
            if (checkVariationValue(variationKey, variationValue, variationMap, scenarioContext)) {
                continue;
            }
            finalValue = finalValue.replace(variationKeyInBraces, variationValue);
        }
        return finalValue;
    }

    private boolean checkVariationValue(final String variationKey,
                                        final String variationValue,
                                        final Map<String, String> variationMap,
                                        final ScenarioContext scenarioContext) {
        if (Objects.isNull(variationValue)) {
            if (isContextValue(variationKey, scenarioContext)) {
                return true;
            }
            throw new IllegalArgumentException(String.format(NO_VALUE_FOUND_FOR_KEY, variationKey, variationMap));
        }
        return false;
    }

    private boolean isContextValue(final String variationKey, final ScenarioContext scenarioContext) {
        return Objects.nonNull(scenarioContext) && scenarioContext.containsKey(variationKey);
    }
}