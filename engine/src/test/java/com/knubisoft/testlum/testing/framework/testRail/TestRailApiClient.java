package com.knubisoft.testlum.testing.framework.testRail;

import com.knubisoft.testlum.testing.framework.testRail.model.Project;
import com.knubisoft.testlum.testing.framework.testRail.model.Run;
import com.knubisoft.testlum.testing.framework.testRail.model.Suite;

import java.util.List;
import java.util.Map;

public interface TestRailApiClient {

    void sendResultsInBatch(int runId, List<Map<String, Object>> results, Map<Integer, String> screenshotsOfUnsuccessfulTests);

    Integer createNewTestRailRun(List<Integer> caseIds);

	Project getProject(Integer projectId);

	Suite getSuite(Integer suiteId);

	Run getRun(Integer runId);

	Run updateRun(Integer runId, List<Integer> caseIds);

	List<Run> getRunsByProject(Integer projectId);
}
