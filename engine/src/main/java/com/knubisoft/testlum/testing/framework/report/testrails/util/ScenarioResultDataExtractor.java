package com.knubisoft.testlum.testing.framework.report.testrails.util;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.report.testrails.constant.TestRailConstants;
import com.knubisoft.testlum.testing.framework.report.testrails.model.GroupedScenarios;
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
                .filter(scenarioResult -> scenarioResult.getOverview().getTestRail().isEnable())
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
                .filter(scenarioResult -> {
                    boolean enable = scenarioResult.getOverview().getTestRail().isEnable();
                    String runId = scenarioResult.getOverview().getTestRail().getTestRailRunId();
                    return enable && parseId(runId, RUN_ID_ATTR) && Integer.parseInt(runId) > 0;
                })
                .collect(Collectors.groupingBy(scenarioResult ->
                        Integer.parseInt(scenarioResult.getOverview().getTestRail().getTestRailRunId())));
    }

    private List<ScenarioResult> getAllScenarioWithoutRunId(final List<ScenarioResult> scenarioResults) {
        return scenarioResults.stream()
                .filter(scenarioResult -> {
                    var testRails = scenarioResult.getOverview().getTestRail();
                    boolean enable = testRails.isEnable();
                    String runId = testRails.getTestRailRunId();
                    String testCase = testRails.getTestCaseId();
                    return enable
                            && (StringUtils.isEmpty(runId) || Integer.parseInt(runId) <= 0)
                            && NumberUtils.isParsable(testCase) && Integer.parseInt(testCase) > 0;
                })
                .collect(Collectors.toList());
    }

    private boolean parseId(final String idStr, final String idType) {
        if (NumberUtils.isParsable(idStr)) {
            return Boolean.TRUE;
        } else {
            String idLogError = CASE_ID_ATTR.equalsIgnoreCase(idType)
                    ? TestRailConstants.CASE_ID_ERROR_RESPONSE
                    : TestRailConstants.RUN_ID_ERROR_RESPONSE;
            log.error(idLogError, idStr);
            return Boolean.FALSE;
        }
    }


}
