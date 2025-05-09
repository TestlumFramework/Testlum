package com.knubisoft.testlum.testing.framework.testRail;

import java.util.List;
import java.util.Map;

public interface TestRailApiClient {
    void sendResultsInBatch(int runId, List<Map<String, Object>> results);
    Integer createNewTestRailRun(List<Integer> caseIds);
}
