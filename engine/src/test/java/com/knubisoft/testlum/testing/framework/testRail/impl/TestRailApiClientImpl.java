package com.knubisoft.testlum.testing.framework.testRail.impl;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.testRail.TestRailApiClient;
import com.knubisoft.testlum.testing.framework.testRail.constant.TestRailConstants;
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

    private final RestTemplate restTemplate;

    @Override
    public void sendResultsInBatch(final int runId, final List<Map<String, Object>> results) {
        TestRailsApi testRails = ConfigProviderImpl.GlobalTestConfigurationProvider.provide().getTestRailsApi();
        String url = testRails.getUrl() + TestRailConstants.ADD_RESULTS_FOR_CASES + runId;
        Map<String, Object> request = new HashMap<>();
        request.put(TestRailConstants.RESULTS, results);
        HttpHeaders headers = buildHeaders(testRails);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        try {
            log.info(TestRailConstants.LOG_SENDING_RESULTS, runId, results.size());
            var response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info(TestRailConstants.LOG_SUCCESS_RESPONSE, runId, response.getBody());
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_ERROR_RESPONSE, runId, e.getMessage(), e);
        }
    }

    private HttpHeaders buildHeaders(final TestRailsApi testRails) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = testRails.getUsername() + COLON + testRails.getApiKey();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set(TestRailConstants.HEADER_AUTHORIZATION,
                TestRailConstants.AUTH_BASIC_PREFIX + encodedAuth);

        return headers;
    }
}
