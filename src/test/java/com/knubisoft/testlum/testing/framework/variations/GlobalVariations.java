package com.knubisoft.testlum.testing.framework.variations;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.parser.CSVParser;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.VARIATIONS_NOT_FOUND;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@UtilityClass
public class GlobalVariations {

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

    public List<Map<String, String>> getVariations(final String fileName,
                                                   final List<String> includedVariations) {
        List<Map<String, String>> variationList = new ArrayList<>();
        if (nonNull(fileName)) {
            variationList = VARIATIONS.get(fileName);
        }
        if (nonNull(includedVariations)) {
            variationList.addAll(addIncludedVariations(includedVariations));
        }
        if (isNull(variationList)) {
            throw new DefaultFrameworkException(VARIATIONS_NOT_FOUND, fileName);
        }
        return variationList;
    }

    private List<Map<String, String>> addIncludedVariations(final List<String> includedVariations) {
        List<Map<String, String>> variationsList = new ArrayList<>();
        if (Objects.nonNull(includedVariations)) {
            includedVariations.forEach(variationFilename -> variationsList.addAll(VARIATIONS.get(variationFilename)));
        }
        return variationsList;
    }

    private class VariationsMap extends HashMap<String, List<Map<String, String>>> {
        private static final long serialVersionUID = 1;
    }
}
