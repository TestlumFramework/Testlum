package com.knubisoft.testlum.testing.framework.report.testrails.impl;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.report.testrails.TestRailApiClient;
import com.knubisoft.testlum.testing.framework.report.testrails.TestRailService;
import com.knubisoft.testlum.testing.framework.report.testrails.model.GroupedScenarios;
import com.knubisoft.testlum.testing.framework.report.testrails.model.ResultRequestDto;
import com.knubisoft.testlum.testing.framework.report.testrails.util.FailureScreenshotCollector;
import com.knubisoft.testlum.testing.framework.report.testrails.util.TestRailResultMapper;
import com.knubisoft.testlum.testing.framework.report.testrails.util.ScenarioResultDataExtractor;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestRailServiceImpl implements TestRailService {

    private final TestRailApiClient testRailApiClient;
    private final TestRailResultMapper formatResult;
    private final ScenarioResultDataExtractor scenarioResultDataExtractor;
    private final FailureScreenshotCollector failureScreenshotCollector;
    private final LogUtil logUtil;

    @Override
    public void generateTestRailReports(final List<ScenarioResult> results) {
        testRailApiClient.validateConnection();
        List<ScenarioResult> testRailScenarios = scenarioResultDataExtractor
                .collectScenarioWithTestRailIntegrations(results);
        if (!testRailScenarios.isEmpty()) {
            sendTestResultToTestRail(testRailScenarios);
        } else {
            logUtil.logEmptyScenariosForTestRails();
        }
    }

    private void sendTestResultToTestRail(final List<ScenarioResult> results) {
        GroupedScenarios grouped = scenarioResultDataExtractor.splitScenariosByRunId(results);
        processWithRunId(grouped.getWithRunId());
        processWithoutRunId(grouped.getWithoutRunId());
    }

    private void processWithRunId(final Map<Integer, List<ScenarioResult>> withRunIdMap) {
        if (withRunIdMap.isEmpty()) {
            return;
        }
        sendGroupedResultsToApi(withRunIdMap);
    }

    private void processWithoutRunId(final List<ScenarioResult> scenariosWithoutRunId) {
        if (scenariosWithoutRunId.isEmpty()) {
            return;
        }
        List<Integer> caseIds = scenarioResultDataExtractor.extractCaseIds(scenariosWithoutRunId);
        if (caseIds.isEmpty()) {
            return;
        }
        testRailApiClient.createNewTestRailRun(caseIds).ifPresent(newTestRunId -> {
            Map<Integer, List<ScenarioResult>> newGroup = Map.of(newTestRunId, scenariosWithoutRunId);
            sendGroupedResultsToApi(newGroup);
        });
    }

    private void sendGroupedResultsToApi(final Map<Integer, List<ScenarioResult>> groupedResults) {
        groupedResults.forEach((runId, scenarioList) -> {
            List<ResultRequestDto> results = buildBatchResults(scenarioList);
            Map<Integer, String> screenshotsOfUnsuccessfulTests = failureScreenshotCollector
                    .getScreenshotsOfUnsuccessfulTests(scenarioList);
            testRailApiClient.sendResultsInBatch(runId, results, screenshotsOfUnsuccessfulTests);
        });
    }

    public List<ResultRequestDto> buildBatchResults(final List<ScenarioResult> scenarioList) {
        return scenarioList.stream()
                .map(formatResult::formatResult)
                .collect(Collectors.toList());
    }
}