package com.knubisoft.testlum.testing.framework.variations;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.parser.CSVParser;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.model.scenario.Repeat;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.VARIATIONS_NOT_FOUND;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
public class GlobalVariationsImpl implements GlobalVariations {

    @Override
    public void process(final Scenario scenario, final File filePath) {
        GlobalVariationsProvider.process(scenario, filePath);
    }

    @Override
    public void process(final Repeat repeat) {
        GlobalVariationsProvider.process(repeat);
    }

    @Override
    public List<Map<String, String>> getVariations(final String fileName) {
        return GlobalVariationsProvider.getVariations(fileName);
    }

    @Override
    public String getValue(final String variation, final Map<String, String> variationMap) {
        return GlobalVariationsProvider.getValue(variation, variationMap);
    }

    @UtilityClass
    public static class GlobalVariationsProvider {

        private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
        private static final String NO_VALUE_FOUND_FOR_KEY = "Unable to find value for key <%s>. Available keys: %s";
        private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);
        private static final VariationsMap VARIATIONS = new VariationsMap();

        private static final CSVParser CSV_PARSER = new CSVParser();
        private static final VariationsValidator VARIATIONS_VALIDATOR = new VariationsValidator();

        public void process(final Scenario scenario, final File filePath) {
            String fileName = scenario.getSettings().getVariations();
            List<Map<String, String>> variationList = VARIATIONS.get(fileName);

            if (isNull(variationList)) {
                variationList = CSV_PARSER.parseVariations(fileName);
                VARIATIONS.putIfAbsent(fileName, variationList);
            }
            VARIATIONS_VALIDATOR.validateByScenario(variationList, scenario, filePath);
        }

        public void process(final Repeat repeat) {
            if (StringUtils.isNotBlank(repeat.getVariations())) {
                String fileName = repeat.getVariations();
                List<Map<String, String>> variationList = VARIATIONS.get(fileName);
                if (isNull(variationList)) {
                    variationList = CSV_PARSER.parseVariations(repeat.getVariations());
                    VARIATIONS.putIfAbsent(repeat.getVariations(), variationList);
                }
            }
        }

        public List<Map<String, String>> getVariations(final String fileName) {
            List<Map<String, String>> variationList = VARIATIONS.get(fileName);
            if (isNull(variationList)) {
                throw new DefaultFrameworkException(VARIATIONS_NOT_FOUND, fileName);
            }
            return variationList;
        }

        private static class VariationsMap extends HashMap<String, List<Map<String, String>>> {
            private static final long serialVersionUID = 1;
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
            if (isNull(variationValue)) {
                if (isContextValue(variationKey, scenarioContext)) {
                    return true;
                }
                throw new IllegalArgumentException(String.format(NO_VALUE_FOUND_FOR_KEY, variationKey, variationMap));
            }
            return false;
        }

        private boolean isContextValue(final String variationKey, final ScenarioContext scenarioContext) {
            if (nonNull(scenarioContext)) {
                try {
                    scenarioContext.inject(variationKey);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }
    }
}
