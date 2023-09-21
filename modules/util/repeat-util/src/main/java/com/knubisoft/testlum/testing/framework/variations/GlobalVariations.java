package com.knubisoft.testlum.testing.framework.variations;

import com.knubisoft.testlum.testing.model.scenario.Repeat;
import com.knubisoft.testlum.testing.model.scenario.Scenario;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface GlobalVariations {

    void process(Scenario scenario, File filePath);

    void process(Repeat repeat);

    List<Map<String, String>> getVariations(String fileName);

    String getVariationValue(String variation, Map<String, String> variationMap);
}
