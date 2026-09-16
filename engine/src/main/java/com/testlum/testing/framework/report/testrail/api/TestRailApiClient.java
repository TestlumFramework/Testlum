package com.testlum.testing.framework.report.testrail.api;


import com.testlum.testing.framework.report.testrail.api.dto.ResultRequest;
import com.testlum.testing.framework.report.testrail.api.dto.TestRailDeliveryOutcome;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TestRailApiClient {

    void validateConnection();

    /**
     * Sends one batch of results to the given run and reports whether they were
     * delivered, how many screenshots were attached, or why the batch failed.
     */
    TestRailDeliveryOutcome sendResultsInBatch(int runId, List<ResultRequest> results,
                                               Map<Integer, String> screenshotsOfUnsuccessfulTests);

    Optional<Integer> createNewTestRailRun(List<Integer> caseIds);

    /**
     * Reads every case and maps the value of the given custom
     * field to its case id. Returns an empty map when the cases cannot be fetched.
     */
    Map<String, Integer> fetchCaseIdsByMatchKey(String caseMatchKey);
}
