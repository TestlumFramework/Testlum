package com.knubisoft.testlum.testing.framework.testRail;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;

import java.util.List;
import java.util.Map;

public interface TestRailSenderService {
    void sendGroupedResultsToApi(Map<Integer, List<ScenarioResult>> groupedResults);
}
