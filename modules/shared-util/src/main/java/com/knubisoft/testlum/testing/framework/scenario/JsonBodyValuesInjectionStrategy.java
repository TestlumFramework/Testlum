package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.util.JsonSpecialMarkingsParser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonBodyValuesInjectionStrategy implements ScenarioContextVariationInjectionStrategy{

    private static final String RAW_REGEXP ="(\"raw\"\\s*:\\s*\")(?:[^\"\\\\]|\\\\.)*(\")";
    private static final Pattern RAW_PATTERN = Pattern.compile(RAW_REGEXP);

    @Override
    public boolean isApplicable(String scenarioStepAsString) {
        return hasJsonVariationsInBody(scenarioStepAsString);
    }

    @Override
    public String inject(String scenarioStepAsString, ScenarioContext scenarioContext) {
        String bodyForInjection = JsonSpecialMarkingsParser.buildJson(scenarioContext.getContextMap());
        Matcher matcher = RAW_PATTERN.matcher(scenarioStepAsString);
        return matcher.replaceFirst("$1" + Matcher.quoteReplacement(bodyForInjection) + "$2");
    }

    private boolean hasJsonVariationsInBody(String scenarioStepAsString) {
        return scenarioStepAsString.contains("{{j(");
    }
}
