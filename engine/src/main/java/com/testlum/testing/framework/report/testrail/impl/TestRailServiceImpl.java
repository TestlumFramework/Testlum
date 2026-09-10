package com.testlum.testing.framework.report.testrail.impl;

import com.testlum.testing.framework.report.ScenarioResult;
import com.testlum.testing.framework.report.testrail.TestRailApiClient;
import com.testlum.testing.framework.report.testrail.TestRailService;
import com.testlum.testing.framework.report.testrail.model.GroupedScenarios;
import com.testlum.testing.framework.report.testrail.model.ResultRequestDto;
import com.testlum.testing.framework.report.testrail.model.ScenarioCase;
import com.testlum.testing.framework.report.testrail.util.TestRailFailedScreenshotCollector;
import com.testlum.testing.framework.report.testrail.util.ScenarioResultDataExtractor;
import com.testlum.testing.framework.report.testrail.util.TestRailCaseIdResolver;
import com.testlum.testing.framework.report.testrail.util.TestRailResultMapper;
import com.testlum.testing.framework.util.LogUtil;
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
    private final TestRailCaseIdResolver caseIdResolver;
    private final TestRailFailedScreenshotCollector failureScreenshotCollector;
    private final LogUtil logUtil;

    @Override
    public void generateTestRailReports(final List<ScenarioResult> results) {
        testRailApiClient.validateConnection();
        List<ScenarioResult> testRailScenarios = scenarioResultDataExtractor
                .collectScenarioWithTestRailIntegrations(results);
        List<ScenarioCase> scenarioCases = caseIdResolver.resolveCases(testRailScenarios);
        if (!scenarioCases.isEmpty()) {
            sendTestResultToTestRail(scenarioCases);
        } else {
            logUtil.logEmptyScenariosForTestRails();
        }
    }

    private void sendTestResultToTestRail(final List<ScenarioCase> scenarioCases) {
        GroupedScenarios grouped = scenarioResultDataExtractor.splitScenariosByRunId(scenarioCases);
        processWithRunId(grouped.getWithRunId());
        processWithoutRunId(grouped.getWithoutRunId());
    }

    private void processWithRunId(final Map<Integer, List<ScenarioCase>> withRunIdMap) {
        if (withRunIdMap.isEmpty()) {
            return;
        }
        sendGroupedResultsToApi(withRunIdMap);
    }

    private void processWithoutRunId(final List<ScenarioCase> scenariosWithoutRunId) {
        if (scenariosWithoutRunId.isEmpty()) {
            return;
        }
        List<Integer> caseIds = scenarioResultDataExtractor.extractCaseIds(scenariosWithoutRunId);
        if (caseIds.isEmpty()) {
            return;
        }
        testRailApiClient.createNewTestRailRun(caseIds).ifPresent(newTestRunId -> {
            Map<Integer, List<ScenarioCase>> newGroup = Map.of(newTestRunId, scenariosWithoutRunId);
            sendGroupedResultsToApi(newGroup);
        });
    }

    private void sendGroupedResultsToApi(final Map<Integer, List<ScenarioCase>> groupedResults) {
        groupedResults.forEach((runId, scenarioList) -> {
            List<ResultRequestDto> results = buildBatchResults(scenarioList);
            Map<Integer, String> screenshotsOfUnsuccessfulTests = failureScreenshotCollector
                    .getScreenshotsOfUnsuccessfulTests(scenarioList);
            testRailApiClient.sendResultsInBatch(runId, results, screenshotsOfUnsuccessfulTests);
        });
    }

    public List<ResultRequestDto> buildBatchResults(final List<ScenarioCase> scenarioList) {
        return scenarioList.stream()
                .map(formatResult::formatResult)
                .collect(Collectors.toList());
    }
}
