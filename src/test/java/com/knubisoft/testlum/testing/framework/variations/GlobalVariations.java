package com.knubisoft.testlum.testing.framework.variations;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.parser.CSVParser;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NO_VALUE_FOUND_FOR_KEY;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.VARIATIONS_NOT_FOUND;
import static java.util.Objects.isNull;

@UtilityClass
public class GlobalVariations {

    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);
    private static final VariationsMap VARIATIONS = new VariationsMap();

    private static final CSVParser CSV_PARSER = new CSVParser();
    private static final VariationsValidator VARIATIONS_VALIDATOR = new VariationsValidator();

    public void process(final Scenario scenario, final File filePath) {
        String fileName = scenario.getVariations();
        List<Map<String, String>> variationList = VARIATIONS.get(fileName);

        if (isNull(variationList)) {
            variationList = CSV_PARSER.parseVariations(fileName);
            VARIATIONS.putIfAbsent(fileName, variationList);
        }
        VARIATIONS_VALIDATOR.validateByScenario(variationList, scenario, filePath);
    }

    public List<Map<String, String>> getVariations(final String fileName) {
        List<Map<String, String>> variationList = VARIATIONS.get(fileName);
        if (isNull(variationList)) {
            throw new DefaultFrameworkException(VARIATIONS_NOT_FOUND, fileName);
        }
        return variationList;
    }

    private class VariationsMap extends HashMap<String, List<Map<String, String>>> {
        private static final long serialVersionUID = 1;
    }

    public String getVariationValue(final String variationKey, final Map<String, String> variationMap) {
        if (StringUtils.isBlank(variationKey)) {
            return variationKey;
        }
        Matcher m = ROUTE_PATTERN.matcher(variationKey);
        return getVariationFromMap(variationKey, m, variationMap);
    }

    private String getVariationFromMap(final String variationKey,
                                       final Matcher m,
                                       final Map<String, String> variationMap) {
        String finalValue = variationKey;
        while (m.find()) {
            String variationName = m.group(1);
            String variationNameInBraces = m.group(0);
            String variationValue = variationMap.get(variationName);
            if (isNull(variationValue)) {
                throw new IllegalArgumentException(String.format(NO_VALUE_FOUND_FOR_KEY, variationKey, variationMap));
            }
            finalValue = finalValue.replace(variationNameInBraces, variationValue);
        }
        return finalValue;
    }
}
