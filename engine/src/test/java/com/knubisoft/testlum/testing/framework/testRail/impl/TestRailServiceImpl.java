package com.knubisoft.testlum.testing.framework.testRail.impl;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
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

    @Override
    public void sendTestResultToTestRail(final List<ScenarioResult> results) {
        GroupedScenarios grouped = TestRailUtil.splitScenariosByRunId(results);
        Map<Integer, List<ScenarioResult>> withRunId = grouped.getWithRunId();
//        senderService.sendGroupedResultsToApi(withRunId);
    }
}
