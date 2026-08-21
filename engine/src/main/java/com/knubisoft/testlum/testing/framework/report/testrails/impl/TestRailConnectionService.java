package com.knubisoft.testlum.testing.framework.report.testrails.impl;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.testrails.constant.TestRailConstants;
import com.knubisoft.testlum.testing.model.global_config.TestRailReports;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COLON;

@Slf4j
public class TestRailConnectionService {

    private final TestRailReports testRails;
    private final RestTemplate restTemplate;
    private final ApiEndpoints endpoints;

    public TestRailConnectionService(final TestRailReports testRails, final RestTemplate restTemplate) {
        this.testRails = testRails;
        this.restTemplate = restTemplate;
        this.endpoints = new ApiEndpoints(testRails);
    }

    public void validateConnection() {
        String url = endpoints.getFetchProjectsEndpoint();
        HttpEntity<Void> httpEntity = new HttpEntity<>(authHeaders());
        try {
            log.info(TestRailConstants.LOG_VALIDATE_CONNECTION, url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info(TestRailConstants.LOG_CONNECTION_SUCCESSFUL);
            } else {
                throw new DefaultFrameworkException(TestRailConstants.LOG_CONNECTION_FAILED, response.getStatusCode());
            }
        } catch (Exception e) {
            throw new DefaultFrameworkException(TestRailConstants.LOG_CONNECTION_ERROR, e.getMessage(), e);
        }
    }

    public HttpHeaders buildHeaders() {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = testRails.getUsername() + COLON + testRails.getApiKey();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set(TestRailConstants.HEADER_AUTHORIZATION,
                TestRailConstants.AUTH_BASIC_PREFIX + encodedAuth);

        return headers;
    }

    public ApiEndpoints endpoints() {
        return endpoints;
    }

    public static class ApiEndpoints {

        private static final String API_SUFFIX = "index.php?/api/v2/";

        private static final String GET_PROJECTS_URL = "get_projects";
        private static final String ADD_RESULTS_FOR_CASES_URL = "add_results_for_cases/";
        private static final String CREATE_NEW_TEST_RUN_URL = "add_run/";
        private static final String GET_TEST_URL = "get_test/";
        private static final String ADD_ATTACHMENT_TO_RESULT_URL = "add_attachment_to_result/";

        private final TestRailReports testRails;

        public ApiEndpoints(final TestRailReports testRails) {
            this.testRails = testRails;
        }

        public String getFetchProjectsEndpoint() {
            return baseUrl() + GET_PROJECTS_URL;
        }

        public String getAddResultsForCaseEndpoint(final int runId) {
            return baseUrl() + ADD_RESULTS_FOR_CASES_URL + runId;
        }

        public String getCreateTextRunEndpoint(final String projectId) {
            return baseUrl() + CREATE_NEW_TEST_RUN_URL + projectId;
        }

        public String getFetchTestEndpoint(final String testId) {
            return baseUrl() + GET_TEST_URL + testId;
        }

        public String getAddAttachmentEndpoint(final int resultId) {
            return baseUrl() + ADD_ATTACHMENT_TO_RESULT_URL + resultId;
        }

        private String baseUrl() {
            String url = testRails.getUrl().trim();

            while (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            if (!url.contains("index.php")) {
                return url + "/" + API_SUFFIX;
            }
            return url + "/";
        }

    }

}
