package com.testlum.testing.framework.report.testrail.util;

import com.testlum.testing.framework.report.ScenarioResult;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.model.GroupedScenarios;
import com.testlum.testing.framework.report.testrail.model.ScenarioCase;
import com.testlum.testing.model.scenario.TestRail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ScenarioResultDataExtractor {

    public List<ScenarioResult> collectScenarioWithTestRailIntegrations(final List<ScenarioResult> allScenarioResults) {
        return allScenarioResults.stream()
                .filter(scenarioResult -> {
                    TestRail testRail = scenarioResult.getOverview().getTestRail();
                    return Objects.nonNull(testRail) && Boolean.TRUE.equals(testRail.isEnabled());
                })
                .collect(Collectors.toList());
    }

    public GroupedScenarios splitScenariosByRunId(final List<ScenarioCase> scenarioCases) {
        Map<Integer, List<ScenarioCase>> withRunId = scenarioCases.stream()
                .filter(this::isRunIdValid)
                .collect(Collectors.groupingBy(this::runIdOf));
        List<ScenarioCase> withoutRunId = scenarioCases.stream()
                .filter(scenarioCase -> !isRunIdValid(scenarioCase))
                .collect(Collectors.toList());
        return new GroupedScenarios(withRunId, withoutRunId);
    }

    public List<Integer> extractCaseIds(final List<ScenarioCase> scenarioCases) {
        return scenarioCases.stream()
                .map(ScenarioCase::caseId)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isRunIdValid(final ScenarioCase scenarioCase) {
        String runId = scenarioCase.scenarioResult().getOverview().getTestRail().getTestRailRunId();
        if (StringUtils.isBlank(runId)) {
            return Boolean.FALSE;
        }
        if (!NumberUtils.isParsable(runId.trim())) {
            log.error(TestRailConstants.RUN_ID_ERROR_RESPONSE, runId);
            return Boolean.FALSE;
        }
        return Integer.parseInt(runId.trim()) > 0;
    }

    private int runIdOf(final ScenarioCase scenarioCase) {
        return Integer.parseInt(scenarioCase.scenarioResult().getOverview().getTestRail().getTestRailRunId().trim());
    }
}
