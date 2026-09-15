package com.testlum.testing.framework.report.testrail;


import com.testlum.testing.framework.report.testrail.model.ResultRequestDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TestRailApiClient {

    void validateConnection();

    void sendResultsInBatch(int runId, List<ResultRequestDto> results,
                            Map<Integer, String> screenshotsOfUnsuccessfulTests);

    Optional<Integer> createNewTestRailRun(List<Integer> caseIds);

    /**
     * Reads every case and maps the value of the given custom
     * field to its case id. Returns an empty map when the cases cannot be fetched.
     */
    Map<String, Integer> fetchCaseIdsByMatchKey(String caseMatchKey);
}
