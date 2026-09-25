package com.testlum.testing.framework.report.testrail.api.impl;

import com.testlum.testing.framework.report.testrail.api.TestRailApiClient;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.api.dto.ResultRequest;
import com.testlum.testing.framework.report.testrail.api.dto.TestRailDeliveryOutcome;
import com.testlum.testing.framework.report.testrail.api.dto.RunRequest;
import com.testlum.testing.framework.report.testrail.summary.util.TestRailErrorDescriber;
import com.testlum.testing.framework.report.testrail.api.util.TestRailResponseJsonDeserializer;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.TestRailReports;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TestRailApiClientImpl implements TestRailApiClient {

    private static final int CASES_PAGE_LIMIT = Integer.parseInt(TestRailConstants.CASES_PAGE_LIMIT);
    private final TestRailReports testRails;
    private final TestRailAttachmentApiClient attachmentApiClient;
    private final TestRailResponseJsonDeserializer jsonDeserializer;
    private final TestRailErrorDescriber errorDescriber;
    private final TestRailConnectionService connectionService;
    private final RestTemplate restTemplate;

    public TestRailApiClientImpl(final GlobalTestConfiguration globalTestConfiguration,
                                 final TestRailAttachmentApiClient attachmentApiClient,
                                 final TestRailResponseJsonDeserializer jsonDeserializer,
                                 final TestRailErrorDescriber errorDescriber,
                                 final RestTemplate restTemplate) {
        this.testRails = globalTestConfiguration.getReport().getExtentReports().getTestRailReports();
        this.attachmentApiClient = attachmentApiClient;
        this.jsonDeserializer = jsonDeserializer;
        this.errorDescriber = errorDescriber;
        this.restTemplate = restTemplate;
        this.connectionService = new TestRailConnectionService(testRails, restTemplate);
    }

    @Override
    public Optional<String> validateConnection() {
        try {
            connectionService.validateConnection();
            return Optional.empty();
        } catch (Exception e) {
            String reason = errorDescriber.describe(e);
            log.error(TestRailConstants.LOG_CONNECTION_ERROR, reason);
            return Optional.of(reason);
        }
    }

    @Override
    public TestRailDeliveryOutcome sendResultsInBatch(final int runId, final List<ResultRequest> results,
                                                      final Map<Integer, String> screenshotsOfUnsuccessfulTests) {
        String url = connectionService.endpoints().getAddResultsForCaseEndpoint(runId);
        HttpEntity<Map<String, Object>> entity = prepareSendHttpRequest(results);
        try {
            log.debug(TestRailConstants.LOG_SENDING_RESULTS, runId, results.size());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.debug(TestRailConstants.LOG_SUCCESS_RESPONSE, runId, response.getBody());
            return TestRailDeliveryOutcome.delivered(
                    attachScreenshots(response.getBody(), screenshotsOfUnsuccessfulTests));
        } catch (Exception e) {
            String reason = errorDescriber.describe(e);
            log.debug(TestRailConstants.LOG_ERROR_RESPONSE, runId, reason, e);
            return TestRailDeliveryOutcome.failed(reason);
        }
    }

    private int attachScreenshots(final String responseBody, final Map<Integer, String> screenshots) {
        if (!screenshotsEnabled() || screenshots.isEmpty()) {
            return 0;
        }
        return attachmentApiClient.attachScreenshotsForFailedScenarios(responseBody, screenshots);
    }

    private HttpEntity<Map<String, Object>> prepareSendHttpRequest(final List<ResultRequest> results) {
        Map<String, Object> request = new HashMap<>();
        request.put(TestRailConstants.RESULTS, results);
        HttpHeaders headers = connectionService.buildHeaders();
        return new HttpEntity<>(request, headers);
    }

    @Override
    public Optional<Integer> createNewTestRailRun(final List<Integer> caseIds) {
        String url = connectionService.endpoints().getCreateTextRunEndpoint(testRails.getProjectId());
        HttpEntity<RunRequest> entity = buildTestRunHttpEntity(caseIds);
        try {
            log.debug(TestRailConstants.LOG_CREATING_TEST_RUN, testRails.getDefaultRunName(), caseIds.size());
            ResponseEntity<RunRequest> response = restTemplate.exchange(url, HttpMethod.POST, entity, RunRequest.class);
            return fetchIdFromResponse(response);
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_TEST_RUN_CREATION_FAILED,
                    testRails.getDefaultRunName(), errorDescriber.describe(e));
            return Optional.empty();
        }
    }

    private HttpEntity<RunRequest> buildTestRunHttpEntity(final List<Integer> caseIds) {
        RunRequest request = RunRequest.builder()
                .name(testRails.getDefaultRunName())
                .description(testRails.getDefaultRunDescription())
                .includeAll(false)
                .caseIds(caseIds).build();
        HttpHeaders headers = connectionService.buildHeaders();
        return new HttpEntity<>(request, headers);
    }

    private Optional<Integer> fetchIdFromResponse(final ResponseEntity<RunRequest> response) {
        RunRequest body = response.getBody();
        if (body != null && body.getId() != null) {
            Integer id = body.getId();
            log.debug(TestRailConstants.LOG_TEST_RUN_CREATED, testRails.getDefaultRunName(), id);
            return Optional.of(id);
        }
        return Optional.empty();
    }

    @Override
    public Map<String, Integer> fetchCaseIdsByMatchKey(final String caseMatchKey) {
        Map<String, List<Integer>> allCaseIdsByMatchKeyValue = new LinkedHashMap<>();
        try {
            collectAllCasePages(caseMatchKey, allCaseIdsByMatchKeyValue);
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_FETCHING_CASES_FAILED, testRails.getProjectId(),
                    errorDescriber.describe(e));
            return Map.of();
        }
        Map<String, Integer> caseIdsByMatchKeyValue = new HashMap<>();
        allCaseIdsByMatchKeyValue.forEach((matchKeyValue, caseIds) ->
                caseIdsByMatchKeyValue.put(matchKeyValue, pickFirstCaseId(caseMatchKey, matchKeyValue, caseIds)));
        log.debug(TestRailConstants.LOG_CASES_FETCHED, caseIdsByMatchKeyValue.size(), caseMatchKey);
        return caseIdsByMatchKeyValue;
    }

    private Integer pickFirstCaseId(final String caseMatchKey, final String matchKeyValue,
                                    final List<Integer> caseIds) {
        Integer firstCaseId = caseIds.get(0);
        if (caseIds.size() > 1) {
            String joinedCaseIds = caseIds.stream().map(String::valueOf).collect(Collectors.joining(", "));
            log.error(TestRailConstants.CASE_MATCH_KEY_VALUE_DUPLICATED,
                    caseMatchKey, matchKeyValue, joinedCaseIds, firstCaseId);
        }
        return firstCaseId;
    }

    private void collectAllCasePages(final String caseMatchKey, final Map<String, List<Integer>> target) {
        HttpEntity<Void> entity = new HttpEntity<>(connectionService.buildHeaders());
        int offset = 0;
        int fetched;
        do {
            String url = connectionService.endpoints().getCasesEndpoint(testRails.getProjectId(), offset);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            fetched = jsonDeserializer.collectCaseIdsByMatchKey(response.getBody(), caseMatchKey, target);
            offset += CASES_PAGE_LIMIT;
        } while (fetched == CASES_PAGE_LIMIT);
    }

    private boolean screenshotsEnabled() {
        return testRails != null
                && testRails.isEnabled()
                && testRails.isAddScreenshotForFailure();
    }
}