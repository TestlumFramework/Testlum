package com.knubisoft.testlum.testing.framework.report.testrails;

import com.knubisoft.testlum.testing.framework.report.testrails.model.ResultRequestDto;

import java.util.List;
import java.util.Map;

public interface TestRailApiClient {

    void validateConnection();

    void sendResultsInBatch(int runId, List<ResultRequestDto> results, Map<Integer, String> screenshotsOfUnsuccessfulTests);

    Integer createNewTestRailRun(List<Integer> caseIds);
}
