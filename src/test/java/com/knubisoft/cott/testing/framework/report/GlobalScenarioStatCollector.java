package com.knubisoft.cott.testing.framework.report;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GlobalScenarioStatCollector {

    private final List<ScenarioResult> results = new ArrayList<>();

    public ScenarioResult addAndReturn(final ScenarioResult result) {
        results.add(result);
        return result;
    }

    public ReCalculatedInfo recalculate() {
        return new ReCalculatedInfo();
    }


    public static class ReCalculatedInfo {

    }
}
