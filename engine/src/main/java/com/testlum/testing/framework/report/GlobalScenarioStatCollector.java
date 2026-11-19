package com.testlum.testing.framework.report;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Service
public class GlobalScenarioStatCollector {

    // Scenarios run in parallel and each adds its result when it ends.
    private final List<ScenarioResult> results = new CopyOnWriteArrayList<>();

    public void addResult(final ScenarioResult result) {
        results.add(result);
    }

    public ReCalculatedInfo recalculate() {
        return new ReCalculatedInfo();
    }


    public static class ReCalculatedInfo {

    }
}
