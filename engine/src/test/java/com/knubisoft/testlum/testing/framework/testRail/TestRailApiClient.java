package com.knubisoft.testlum.testing.framework.testRail;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;

import java.util.List;
import java.util.Map;

public interface TestRailApiClient {
    void sendResultsInBatch(int runId, List<Map<String, Object>> results, Map<Integer, String> screenshotOfLastUnsuccessfulStep);
    Integer createNewTestRailRun(List<Integer> caseIds);
}
