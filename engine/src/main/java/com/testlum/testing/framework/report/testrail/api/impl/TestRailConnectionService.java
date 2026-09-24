package com.testlum.testing.framework.report.testrail.api.impl;

import com.testlum.testing.framework.constant.DelimiterConstant;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.api.util.TestRailUrlFormatter;
import com.testlum.testing.model.global_config.TestRailReports;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class TestRailConnectionService {

    private final TestRailReports testRails;
    private final RestTemplate restTemplate;
    private ApiEndpoints endpoints;

    public TestRailConnectionService(final TestRailReports testRails, final RestTemplate restTemplate) {
        this.testRails = testRails;
        this.restTemplate = restTemplate;
    }

    public void validateConnection() {
        String url = endpoints().getFetchProjectsEndpoint();
        HttpEntity<Void> httpEntity = new HttpEntity<>(authHeaders());
        log.info(TestRailConstants.LOG_VALIDATE_CONNECTION, url);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new DefaultFrameworkException(TestRailConstants.CONNECTION_FAILED_STATUS,
                    response.getStatusCode().value());
        }
        log.info(TestRailConstants.LOG_CONNECTION_SUCCESSFUL);
    }

    public HttpHeaders buildHeaders() {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = testRails.getUsername() + DelimiterConstant.COLON + testRails.getApiKey();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set(TestRailConstants.HEADER_AUTHORIZATION,
                TestRailConstants.AUTH_BASIC_PREFIX + encodedAuth);

        return headers;
    }

    /**
     * Endpoints are built on first use, as the TestRail section may be absent from the global config.
     */
    public ApiEndpoints endpoints() {
        if (endpoints == null) {
            endpoints = new ApiEndpoints(testRails);
        }
        return endpoints;
    }

    public static class ApiEndpoints {

        private static final String GET_PROJECTS_URL = "get_projects";
        private static final String ADD_RESULTS_FOR_CASES_URL = "add_results_for_cases/";
        private static final String CREATE_NEW_TEST_RUN_URL = "add_run/";
        private static final String GET_TEST_URL = "get_test/";
        private static final String GET_CASES_URL = "get_cases/";
        private static final String ADD_ATTACHMENT_TO_RESULT_URL = "add_attachment_to_result/";

        private final String baseUrl;

        private ApiEndpoints(final TestRailReports testRails) {
            this.baseUrl = TestRailUrlFormatter.format(testRails.getUrl());
        }

        public String getFetchProjectsEndpoint() {
            return baseUrl + GET_PROJECTS_URL;
        }

        public String getAddResultsForCaseEndpoint(final int runId) {
            return baseUrl + ADD_RESULTS_FOR_CASES_URL + runId;
        }

        public String getCreateTextRunEndpoint(final String projectId) {
            return baseUrl + CREATE_NEW_TEST_RUN_URL + StringUtils.trimToEmpty(projectId);
        }

        public String getFetchTestEndpoint(final String testId) {
            return baseUrl + GET_TEST_URL + testId;
        }

        public String getCasesEndpoint(final String projectId, final int offset) {
            return baseUrl + GET_CASES_URL + StringUtils.trimToEmpty(projectId)
                    + "&limit=" + TestRailConstants.CASES_PAGE_LIMIT + "&offset=" + offset;
        }

        public String getAddAttachmentEndpoint(final int resultId) {
            return baseUrl + ADD_ATTACHMENT_TO_RESULT_URL + resultId;
        }

    }

}