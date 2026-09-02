package com.testlum.testing.framework.report.testrail.util;

import com.testlum.testing.framework.report.ScenarioResult;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.model.GroupedScenarios;
import com.testlum.testing.model.scenario.TestRail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ScenarioResultDataExtractor {

    private static final String CASE_ID_ATTR = "caseId";
    private static final String RUN_ID_ATTR = "runId";

    public List<ScenarioResult> collectScenarioWithTestRailIntegrations(final List<ScenarioResult> allScenarioResults) {
        return allScenarioResults.stream()
                .filter(scenarioResult -> scenarioResult.getOverview().getTestRail().isEnabled())
                .collect(Collectors.toList());
    }

    public GroupedScenarios splitScenariosByRunId(final List<ScenarioResult> scenarioResults) {
        Map<Integer, List<ScenarioResult>> withRunId = getAllScenarioWithRunId(scenarioResults);
        List<ScenarioResult> withoutRunId = getAllScenarioWithoutRunId(scenarioResults);
        return new GroupedScenarios(withRunId, withoutRunId);
    }

    public List<Integer> extractCaseIds(final List<ScenarioResult> scenarioResults) {
        return scenarioResults.stream()
                .map(scenarioResult -> scenarioResult.getOverview().getTestRail().getTestCaseId())
                .filter(id -> parseId(id, CASE_ID_ATTR))
                .map(Integer::parseInt)
                .filter(caseId -> caseId > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<Integer, List<ScenarioResult>> getAllScenarioWithRunId(final List<ScenarioResult> scenarioResults) {
        return scenarioResults.stream()
                .filter(this::isRunIdValid)
                .collect(Collectors.groupingBy(scenarioResult ->
                        Integer.parseInt(scenarioResult.getOverview().getTestRail().getTestRailRunId())));
    }

    private boolean isRunIdValid(final ScenarioResult scenarioResult) {
        boolean enable = scenarioResult.getOverview().getTestRail().isEnabled();
        String runId = scenarioResult.getOverview().getTestRail().getTestRailRunId();
        return enable && parseId(runId, RUN_ID_ATTR) && Integer.parseInt(runId) > 0;
    }

    private List<ScenarioResult> getAllScenarioWithoutRunId(final List<ScenarioResult> scenarioResults) {
        return scenarioResults.stream()
                .filter(this::isScenarioWithoutRunId)
                .collect(Collectors.toList());
    }

    private boolean isScenarioWithoutRunId(final ScenarioResult scenarioResult) {
        TestRail testRail = scenarioResult.getOverview().getTestRail();
        boolean enable = testRail.isEnabled();
        String runId = testRail.getTestRailRunId();
        String testCase = testRail.getTestCaseId();
        return enable
                && (StringUtils.isEmpty(runId) || Integer.parseInt(runId) <= 0)
                && NumberUtils.isParsable(testCase) && Integer.parseInt(testCase) > 0;
    }

    private boolean parseId(final String id, final String attrName) {
        if (NumberUtils.isParsable(id)) {
            return Boolean.TRUE;
        } else {
            String idLogError = CASE_ID_ATTR.equalsIgnoreCase(attrName)
                    ? TestRailConstants.CASE_ID_ERROR_RESPONSE
                    : TestRailConstants.RUN_ID_ERROR_RESPONSE;
            log.error(idLogError, id);
            return Boolean.FALSE;
        }
    }
}