package com.knubisoft.testlum.testing.framework.testRail.impl;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.testRail.TestRailApiClient;
import com.knubisoft.testlum.testing.framework.testRail.constant.TestRailConstants;
import com.knubisoft.testlum.testing.framework.testRail.util.TestRailUtil;
import com.knubisoft.testlum.testing.model.global_config.TestRailsApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COLON;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestRailApiClientImpl implements TestRailApiClient {

    private final TestRailsApi testRails =
            ConfigProviderImpl.GlobalTestConfigurationProvider.provide().getTestRailsApi();

    private final RestTemplate restTemplate;

    @Override
    public void sendResultsInBatch(final int runId, final List<Map<String, Object>> results) {
        String url = testRails.getUrl() + TestRailConstants.ADD_RESULTS_FOR_CASES_URL + runId;
        Map<String, Object> request = new HashMap<>();
        request.put(TestRailConstants.RESULTS, results);
        HttpHeaders headers = buildHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        try {
            log.info(TestRailConstants.LOG_SENDING_RESULTS, runId, results.size());
            var response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info(TestRailConstants.LOG_SUCCESS_RESPONSE, runId, response.getBody());
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_ERROR_RESPONSE, runId, e.getMessage(), e);
        }
    }

    @Override
    public Integer createNewTestRailRun(final List<Integer> caseIds) {
        String url = testRails.getUrl() + TestRailConstants.CREATE_NEW_TEST_RUN_URL + testRails.getProjectId();
        Map<String, Object> request = TestRailUtil.buildCreateTestRunRequest(testRails, caseIds);
        HttpHeaders headers = buildHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        try {
            log.info(TestRailConstants.LOG_CREATING_TEST_RUN, testRails.getDefaultRunName(), caseIds.size());
            var response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey(TestRailConstants.ID)) {
                Integer id = (Integer) body.get(TestRailConstants.ID);
                log.info(TestRailConstants.LOG_TEST_RUN_CREATED, testRails.getDefaultRunName(), id);
                return id;
            }
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_TEST_RUN_CREATION_FAILED, testRails.getDefaultRunName(), e.getMessage(), e);
        }
        return null;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = testRails.getUsername() + COLON + testRails.getApiKey();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set(TestRailConstants.HEADER_AUTHORIZATION,
                TestRailConstants.AUTH_BASIC_PREFIX + encodedAuth);

        return headers;
    }
}
