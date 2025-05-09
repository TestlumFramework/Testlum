package com.knubisoft.testlum.testing.framework.testRail.util;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.testRail.constant.TestRailConstants;
import com.knubisoft.testlum.testing.framework.testRail.model.GroupedScenarios;
import com.knubisoft.testlum.testing.model.global_config.TestRailsApi;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                    var testRails = scenarioResult.getOverview().getTestRails();
                    Boolean enable = testRails.isEnable();
                    Integer runId = testRails.getTestRailRunId();
                    Integer testCase = testRails.getTestCaseId();
                    return Boolean.TRUE.equals(enable)
                            && (runId == null || runId <= 0)
                            && (testCase != null && testCase > 0);
                })
                .collect(Collectors.toList());
    }

    public List<Integer> extractCaseIds(List<ScenarioResult> scenarioResults) {
        return scenarioResults.stream()
                .map(scenarioResult -> scenarioResult.getOverview().getTestRails().getTestCaseId())
                .filter(Objects::nonNull)
                .filter(caseId -> caseId > 0)
                .distinct()
                .collect(Collectors.toList());
    }


    private Map<Integer, List<ScenarioResult>> getAllScenarioWithRunId(final List<ScenarioResult> scenarioResults) {
        return scenarioResults.stream()
                .filter(scenarioResult -> {
                    Boolean enable = scenarioResult.getOverview().getTestRails().isEnable();
                    Integer runId = scenarioResult.getOverview().getTestRails().getTestRailRunId();
                    return Boolean.TRUE.equals(enable) && runId != null && runId > 0;
                })
                .collect(Collectors.groupingBy(
                        scenarioResult -> scenarioResult.getOverview().getTestRails().getTestRailRunId()
                ));
    }

    public static List<Map<String, Object>> buildBatchResults(final List<ScenarioResult> scenarioList) {
        return scenarioList.stream()
                .map(scenarioResult -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put(TestRailConstants.CASE_ID, scenarioResult.getOverview().getTestRails().getTestCaseId());
                    result.put(TestRailConstants.STATUS_ID, determineStatus(scenarioResult));
                    result.put(TestRailConstants.COMMENT, generateComment(scenarioResult));
                    return result;
                })
                .collect(Collectors.toList());
    }

    private static int determineStatus(final ScenarioResult scenarioResult) {
        return scenarioResult.isSuccess()
                ? TestRailConstants.STATUS_PASSED
                : TestRailConstants.STATUS_FAILED;
    }

    private static String generateComment(final ScenarioResult scenarioResult) {
        String scenarioName = scenarioResult.getOverview().getName();
        String cause = scenarioResult.getCause();

        return (cause != null && !cause.isEmpty())
                ? String.format(TestRailConstants.COMMENT_FAILED_TEMPLATE, cause)
                : String.format(TestRailConstants.COMMENT_PASSED_TEMPLATE, scenarioName);
    }

    public static Map<String, Object> buildCreateTestRunRequest(final TestRailsApi testRails,
                                                                final List<Integer> caseIds) {
        Map<String, Object> request = new HashMap<>();
        request.put(TestRailConstants.RUN_NAME, testRails.getDefaultRunName());
        request.put(TestRailConstants.RUN_DESCRIPTION, testRails.getDefaultRunDescription());
        request.put(TestRailConstants.RUN_INCLUDE_ALL, false);
        request.put(TestRailConstants.RUN_CASE_IDS, caseIds);
        return request;
    }
}
