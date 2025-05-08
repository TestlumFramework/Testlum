package com.knubisoft.testlum.testing.framework.testRail.util;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.testRail.model.GroupedScenarios;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class TestRailUtil {

    public List<ScenarioResult> getScenarioWithTestRailIntegrations(final List<ScenarioResult> allScenarioResults) {
        return allScenarioResults.stream()
                .filter(scenarioResult -> scenarioResult.getOverview().getTestRails().isEnable())
                .collect(Collectors.toList());
    }

    public GroupedScenarios splitScenariosByRunId(final List<ScenarioResult> scenarioResults) {
        Map<Integer, List<ScenarioResult>> withRunId = getAllScenarioWithRunId(scenarioResults);
        List<ScenarioResult> withoutRunId = getAllScenarioWithoutRunId(scenarioResults);
        return new GroupedScenarios(withRunId, withoutRunId);
    }

    private List<ScenarioResult> getAllScenarioWithoutRunId(final List<ScenarioResult> scenarioResults) {
        return scenarioResults.stream()
                .filter(scenarioResult -> {
                    Boolean enable = scenarioResult.getOverview().getTestRails().isEnable();
                    Integer runId = scenarioResult.getOverview().getTestRails().getTestRailRun();
                    return Boolean.TRUE.equals(enable) && (runId == null || runId <= 0);
                })
                .collect(Collectors.toList());
    }

    private Map<Integer, List<ScenarioResult>> getAllScenarioWithRunId(final List<ScenarioResult> scenarioResults) {
        return scenarioResults.stream()
                .filter(scenarioResult -> {
                    Boolean enable = scenarioResult.getOverview().getTestRails().isEnable();
                    Integer runId = scenarioResult.getOverview().getTestRails().getTestRailRun();
                    return Boolean.TRUE.equals(enable) && runId != null && runId > 0;
                })
                .collect(Collectors.groupingBy(
                        scenarioResult -> scenarioResult.getOverview().getTestRails().getTestRailRun()
                ));
    }
}
