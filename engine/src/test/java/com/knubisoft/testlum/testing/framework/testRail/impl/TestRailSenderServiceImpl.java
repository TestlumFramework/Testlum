package com.knubisoft.testlum.testing.framework.testRail.impl;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.testRail.TestRailApiClient;
import com.knubisoft.testlum.testing.framework.testRail.TestRailSenderService;
import com.knubisoft.testlum.testing.framework.testRail.constant.TestRailConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestRailSenderServiceImpl implements TestRailSenderService {

    private final TestRailApiClient testRailApiClient;

    @Override
    public void sendGroupedResultsToApi(final Map<Integer, List<ScenarioResult>> groupedResults) {
        groupedResults.forEach((runId, scenarioList) -> {
            List<Map<String, Object>> results = createResult(scenarioList);
            testRailApiClient.sendResultsInBatch(runId, results);
        });
    }

    private List<Map<String, Object>> createResult(final List<ScenarioResult> scenarioList) {
        return scenarioList.stream()
                .map(scenarioResult -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put(TestRailConstants.CASE_ID, scenarioResult.getOverview().getTestRails().getTestCase());
                    result.put(TestRailConstants.STATUS_ID, determineStatus(scenarioResult));
                    result.put(TestRailConstants.COMMENT, generateComment(scenarioResult));
                    return result;
                })
                .collect(Collectors.toList());
    }

    private int determineStatus(final ScenarioResult scenarioResult) {
        return scenarioResult.isSuccess()
                ? TestRailConstants.STATUS_PASSED
                : TestRailConstants.STATUS_FAILED;
    }

    private String generateComment(final ScenarioResult scenarioResult) {
        String scenarioName = scenarioResult.getOverview().getName();
        String cause = scenarioResult.getCause();

        return (cause != null && !cause.isEmpty())
                ? String.format(TestRailConstants.COMMENT_FAILED_TEMPLATE, cause)
                : String.format(TestRailConstants.COMMENT_PASSED_TEMPLATE, scenarioName);
    }
}
