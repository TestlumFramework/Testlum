package com.knubisoft.testlum.testing.framework.testRail.impl;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.testRail.TestRailApiClient;
import com.knubisoft.testlum.testing.framework.testRail.TestRailSenderService;
import com.knubisoft.testlum.testing.framework.testRail.TestRailService;
import com.knubisoft.testlum.testing.framework.testRail.model.GroupedScenarios;
import com.knubisoft.testlum.testing.framework.testRail.util.TestRailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TestRailServiceImpl implements TestRailService {

    private final TestRailSenderService senderService;
    private final TestRailApiClient testRailApiClient;


    @Override
    public void sendTestResultToTestRail(final List<ScenarioResult> results) {
        GroupedScenarios grouped = TestRailUtil.splitScenariosByRunId(results);
        processWithRunId(grouped.getWithRunId());
        processWithoutRunId(grouped.getWithoutRunId());
    }

    private void processWithRunId(final Map<Integer, List<ScenarioResult>> withRunIdMap) {
        if (withRunIdMap.isEmpty()) {
            return;
        }
        senderService.sendGroupedResultsToApi(withRunIdMap);
    }

    private void processWithoutRunId(final List<ScenarioResult> withoutRunIdList) {
        if (withoutRunIdList.isEmpty()) {
            return;
        }

        List<Integer> caseIds = TestRailUtil.extractCaseIds(withoutRunIdList);
        if (caseIds.isEmpty()) {
            return;
        }

        Integer newTestRunId = testRailApiClient.createNewTestRailRun(caseIds);
        if (newTestRunId != null) {
            Map<Integer, List<ScenarioResult>> newGroup = Map.of(newTestRunId, withoutRunIdList);
            senderService.sendGroupedResultsToApi(newGroup);
        }
    }
}
