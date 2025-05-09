package com.knubisoft.testlum.testing.framework.testRail.impl;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.testRail.TestRailApiClient;
import com.knubisoft.testlum.testing.framework.testRail.TestRailSenderService;
import com.knubisoft.testlum.testing.framework.testRail.util.TestRailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TestRailSenderServiceImpl implements TestRailSenderService {

    private final TestRailApiClient testRailApiClient;

    @Override
    public void sendGroupedResultsToApi(final Map<Integer, List<ScenarioResult>> groupedResults) {
        groupedResults.forEach((runId, scenarioList) -> {
            List<Map<String, Object>> results = TestRailUtil.buildBatchResults(scenarioList);
            testRailApiClient.sendResultsInBatch(runId, results);
        });
    }
}
