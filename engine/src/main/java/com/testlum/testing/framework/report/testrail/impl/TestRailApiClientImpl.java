package com.testlum.testing.framework.report.testrail.impl;

import com.testlum.testing.framework.report.testrail.TestRailApiClient;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.model.ResultRequestDto;
import com.testlum.testing.framework.report.testrail.model.Run;
import com.testlum.testing.framework.report.testrail.util.TestRailResponseJsonDeserializer;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TestRailApiClientImpl implements TestRailApiClient {

    private static final int CASES_PAGE_LIMIT = Integer.parseInt(TestRailConstants.CASES_PAGE_LIMIT);
    private final TestRailReports testRails;
    private final TestRailAttachmentApiClient attachmentApiClient;
    private final TestRailResponseJsonDeserializer jsonDeserializer;
    private final TestRailConnectionService connectionService;
    private final RestTemplate restTemplate;

    public TestRailApiClientImpl(final GlobalTestConfiguration globalTestConfiguration,
                                 final TestRailAttachmentApiClient attachmentApiClient,
                                 final TestRailResponseJsonDeserializer jsonDeserializer,
                                 final RestTemplate restTemplate) {
        this.testRails = globalTestConfiguration.getReport().getExtentReports().getTestRailReports();
        this.attachmentApiClient = attachmentApiClient;
        this.jsonDeserializer = jsonDeserializer;
        this.restTemplate = restTemplate;
        this.connectionService = new TestRailConnectionService(testRails, restTemplate);
    }

    @Override
    public void validateConnection() {
        connectionService.validateConnection();
    }

    @Override
    public void sendResultsInBatch(final int runId, final List<ResultRequestDto> results,
                                   final Map<Integer, String> screenshotsOfUnsuccessfulTests) {
        String url = connectionService.endpoints().getAddResultsForCaseEndpoint(runId);
        HttpEntity<Map<String, Object>> entity = prepareSendHttpRequest(results);
        try {
            log.info(TestRailConstants.LOG_SENDING_RESULTS, runId, results.size());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info(TestRailConstants.LOG_SUCCESS_RESPONSE, runId, response.getBody());
            if (screenshotsEnabled() && !screenshotsOfUnsuccessfulTests.isEmpty()) {
                attachmentApiClient.attachScreenshotsForFailedScenarios(
                        response.getBody(), screenshotsOfUnsuccessfulTests);
            }
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_ERROR_RESPONSE, runId, e.getMessage(), e);
        }
    }

    private HttpEntity<Map<String, Object>> prepareSendHttpRequest(final List<ResultRequestDto> results) {
        Map<String, Object> request = new HashMap<>();
        request.put(TestRailConstants.RESULTS, results);
        HttpHeaders headers = connectionService.buildHeaders();
        return new HttpEntity<>(request, headers);
    }

    @Override
    public Optional<Integer> createNewTestRailRun(final List<Integer> caseIds) {
        String url = connectionService.endpoints().getCreateTextRunEndpoint(testRails.getProjectId());
        HttpEntity<Run> entity = buildTestRunHttpEntity(caseIds);
        try {
            log.info(TestRailConstants.LOG_CREATING_TEST_RUN, testRails.getDefaultRunName(), caseIds.size());
            ResponseEntity<Run> response = restTemplate.exchange(url, HttpMethod.POST, entity, Run.class);
            return fetchIdFromResponse(response);
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_TEST_RUN_CREATION_FAILED, testRails.getDefaultRunName(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    private HttpEntity<Run> buildTestRunHttpEntity(final List<Integer> caseIds) {
        Run request = Run.builder()
                .name(testRails.getDefaultRunName())
                .description(testRails.getDefaultRunDescription())
                .includeAll(false)
                .caseIds(caseIds).build();
        HttpHeaders headers = connectionService.buildHeaders();
        return new HttpEntity<>(request, headers);
    }

    private Optional<Integer> fetchIdFromResponse(final ResponseEntity<Run> response) {
        Run body = response.getBody();
        if (body != null && body.getId() != null) {
            Integer id = body.getId();
            log.info(TestRailConstants.LOG_TEST_RUN_CREATED, testRails.getDefaultRunName(), id);
            return Optional.of(id);
        }
        return Optional.empty();
    }

    @Override
    public Map<String, Integer> fetchCaseIdsByMatchKey(final String caseMatchKey) {
        Map<String, Integer> caseIdsByMatchKeyValue = new HashMap<>();
        try {
            collectAllCasePages(caseMatchKey, caseIdsByMatchKeyValue);
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_FETCHING_CASES_FAILED, testRails.getProjectId(), e.getMessage(), e);
            return Map.of();
        }
        log.info(TestRailConstants.LOG_CASES_FETCHED, caseIdsByMatchKeyValue.size(), caseMatchKey);
        return caseIdsByMatchKeyValue;
    }

    private void collectAllCasePages(final String caseMatchKey, final Map<String, Integer> target) {
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