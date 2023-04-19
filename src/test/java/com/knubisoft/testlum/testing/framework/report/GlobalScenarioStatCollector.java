package com.knubisoft.testlum.testing.framework.report;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Getter
@Service
public class GlobalScenarioStatCollector {

    private final List<ScenarioResult> results = new ArrayList<>();

    public void addResult(final ScenarioResult result) {
        results.add(result);
    }

    public ReCalculatedInfo recalculate() {
        return new ReCalculatedInfo();
    }


    public static class ReCalculatedInfo {

    }
}
