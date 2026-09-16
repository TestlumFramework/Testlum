package com.testlum.testing.framework.report.testrail.service.impl;

import com.testlum.testing.framework.report.ScenarioResult;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.api.TestRailApiClient;
import com.testlum.testing.framework.report.testrail.api.dto.TestRailDeliveryOutcome;
import com.testlum.testing.framework.report.testrail.service.model.GroupedScenarios;
import com.testlum.testing.framework.report.testrail.api.dto.ResultRequest;
import com.testlum.testing.framework.report.testrail.service.model.ScenarioCase;
import com.testlum.testing.framework.report.testrail.service.TestRailService;
import com.testlum.testing.framework.report.testrail.summary.TestRailReportSummary;
import com.testlum.testing.framework.report.testrail.service.util.TestRailFailedScreenshotCollector;
import com.testlum.testing.framework.report.testrail.service.util.ScenarioResultDataExtractor;
import com.testlum.testing.framework.report.testrail.service.util.TestRailCaseIdResolver;
import com.testlum.testing.framework.report.testrail.service.util.TestRailResultMapper;
import com.testlum.testing.framework.report.testrail.summary.TestRailSummaryLogger;
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
    private final TestRailSummaryLogger summaryLogger;
    private final LogUtil logUtil;

    @Override
    public void generateTestRailReports(final List<ScenarioResult> results) {
        testRailApiClient.validateConnection();
        List<ScenarioResult> testRailScenarios = scenarioResultDataExtractor
                .collectScenarioWithTestRailIntegrations(results);
        TestRailReportSummary summary = new TestRailReportSummary();
        List<ScenarioCase> scenarioCases = caseIdResolver.resolveCases(testRailScenarios, summary);
        if (!scenarioCases.isEmpty()) {
            sendTestResultToTestRail(scenarioCases, summary);
        } else {
            logUtil.logEmptyScenariosForTestRails();
        }
        summaryLogger.logSummary(summary);
    }

    private void sendTestResultToTestRail(final List<ScenarioCase> scenarioCases,
                                          final TestRailReportSummary summary) {
        GroupedScenarios grouped = scenarioResultDataExtractor.splitScenariosByRunId(scenarioCases);
        processWithRunId(grouped.getWithRunId(), summary);
        processWithoutRunId(grouped.getWithoutRunId(), summary);
    }

    private void processWithRunId(final Map<Integer, List<ScenarioCase>> withRunIdMap,
                                  final TestRailReportSummary summary) {
        if (withRunIdMap.isEmpty()) {
            return;
        }
        sendGroupedResultsToApi(withRunIdMap, summary);
    }

    private void processWithoutRunId(final List<ScenarioCase> scenariosWithoutRunId,
                                     final TestRailReportSummary summary) {
        if (scenariosWithoutRunId.isEmpty()) {
            return;
        }
        List<Integer> caseIds = scenarioResultDataExtractor.extractCaseIds(scenariosWithoutRunId);
        if (caseIds.isEmpty()) {
            return;
        }
        testRailApiClient.createNewTestRailRun(caseIds).ifPresentOrElse(
                newTestRunId -> sendGroupedResultsToApi(Map.of(newTestRunId, scenariosWithoutRunId), summary),
                () -> markAsNotReported(scenariosWithoutRunId, TestRailConstants.REASON_RUN_NOT_CREATED, summary));
    }

    private void sendGroupedResultsToApi(final Map<Integer, List<ScenarioCase>> groupedResults,
                                         final TestRailReportSummary summary) {
        groupedResults.forEach((runId, scenarioList) ->
                sendRunResults(runId, scenarioList, summary));
    }

    private void sendRunResults(final int runId, final List<ScenarioCase> scenarioList,
                                final TestRailReportSummary summary) {
        List<ResultRequest> results = buildBatchResults(scenarioList);
        Map<Integer, String> screenshotsOfUnsuccessfulTests = failureScreenshotCollector
                .getScreenshotsOfUnsuccessfulTests(scenarioList);
        TestRailDeliveryOutcome outcome = testRailApiClient
                .sendResultsInBatch(runId, results, screenshotsOfUnsuccessfulTests);
        recordOutcome(runId, scenarioList, outcome, summary);
    }

    private void recordOutcome(final int runId, final List<ScenarioCase> scenarioList,
                               final TestRailDeliveryOutcome outcome, final TestRailReportSummary summary) {
        if (!outcome.delivered()) {
            markAsNotReported(scenarioList, outcome.failureReason(), summary);
            return;
        }
        summary.addAttachedScreenshots(outcome.attachedScreenshots());
        scenarioList.forEach(scenarioCase -> summary.addReported(scenarioCase.scenarioResult().getName(),
                scenarioCase.caseId(), runId, scenarioCase.scenarioResult().isSuccess()));
    }

    private void markAsNotReported(final List<ScenarioCase> scenarioList, final String reason,
                                   final TestRailReportSummary summary) {
        scenarioList.forEach(scenarioCase -> summary.addNotReported(scenarioCase.scenarioResult().getName(),
                String.valueOf(scenarioCase.caseId()), reason));
    }

    public List<ResultRequest> buildBatchResults(final List<ScenarioCase> scenarioList) {
        return scenarioList.stream()
                .map(formatResult::formatResult)
                .collect(Collectors.toList());
    }
}
