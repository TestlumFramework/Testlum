package com.knubisoft.testlum.testing.framework.testRail.impl;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.testRail.TestRailApiClient;
import com.knubisoft.testlum.testing.framework.testRail.constant.TestRailConstants;
import com.knubisoft.testlum.testing.framework.testRail.model.Project;
import com.knubisoft.testlum.testing.framework.testRail.model.ResultResponseDto;
import com.knubisoft.testlum.testing.framework.testRail.model.Run;
import com.knubisoft.testlum.testing.framework.testRail.model.Suite;
import com.knubisoft.testlum.testing.framework.testRail.util.TestRailUtil;
import com.knubisoft.testlum.testing.model.global_config.TestRailReports;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

    private final TestRailReports testRails =
            ConfigProviderImpl.GlobalTestConfigurationProvider.provide().getReport().getExtentReports().getTestRailReports();

    private final RestTemplate restTemplate;

    @Override
    public void sendResultsInBatch(final int runId, final List<Map<String, Object>> results,
                                   final Map<Integer, String> screenshotsOfUnsuccessfulTests) {
        String url = testRails.getUrl() + TestRailConstants.ADD_RESULTS_FOR_CASES_URL + runId;
        Map<String, Object> request = new HashMap<>();
        request.put(TestRailConstants.RESULTS, results);
        HttpHeaders headers = buildHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        try {
            log.info(TestRailConstants.LOG_SENDING_RESULTS, runId, results.size());
            var response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info(TestRailConstants.LOG_SUCCESS_RESPONSE, runId, response.getBody());
            if (screenshotsEnabled() && !screenshotsOfUnsuccessfulTests.isEmpty()) {
                attachScreenshotsForFailedScenarios(response.getBody(), screenshotsOfUnsuccessfulTests);
            }
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_ERROR_RESPONSE, runId, e.getMessage(), e);
        }
    }

    private void attachScreenshotsForFailedScenarios(final String responseBody, final Map<Integer, String> screenshotOfLastUnsuccessfulStep) {
        List<ResultResponseDto> resultDTOs = TestRailUtil.extractResultsDTOs(responseBody);
        for (ResultResponseDto resultDTO : resultDTOs) {
            if (resultDTO.getStatusId() != TestRailConstants.STATUS_FAILED) {
                continue;
            }
            String getTestUrl = testRails.getUrl() + TestRailConstants.GET_TEST_URL + resultDTO.getTestId();
            try {
                log.info(TestRailConstants.LOG_FETCHING_TEST, resultDTO.getTestId());
                var getTestResponse = restTemplate.exchange(getTestUrl, HttpMethod.GET, new HttpEntity<>(buildHeaders()), String.class);
                log.info(TestRailConstants.LOG_FETCHING_TEST_SUCCESS_RESPONSE, resultDTO.getTestId(), getTestResponse.getBody());
                Integer caseId = TestRailUtil.extractIdFieldFromJson(getTestResponse.getBody(), "case_id");
                if(caseId != null && caseId > 0) {
                    screenshotOfLastUnsuccessfulStep.entrySet().stream()
                            .filter(attachmentEntry -> attachmentEntry.getKey().equals(caseId))
                            .findFirst()
                            .ifPresent(attachmentEntry -> sendAttachment(resultDTO.getId(), attachmentEntry));
                }
            } catch (Exception e){
                log.error(TestRailConstants.LOG_FETCHING_TEST_ERROR_RESPONSE, resultDTO.getTestId(), e.getMessage(), e);
            }
        }
    }

    private void sendAttachment(Integer resultId, Map.Entry<Integer, String> entry) {
        String addAttachmentUrl = testRails.getUrl() + TestRailConstants.ADD_ATTACHMENT_TO_RESULT_URL + resultId;
        HttpHeaders httpHeaders = buildHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        byte[] imageBytes = Base64.getDecoder().decode(entry.getValue());
        ByteArrayResource resource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return TestRailConstants.ATTACHMENT_DEFAULT_FILENAME;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(TestRailConstants.ATTACHMENT_KEY, resource);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);
        try {
            log.info(TestRailConstants.LOG_SENDING_ATTACHMENT, resource);
            var response = restTemplate.postForEntity(addAttachmentUrl, requestEntity, String.class);
            log.info(TestRailConstants.LOG_ATTACHMENT_SUCCESS_RESPONSE, resultId, response.getBody());
        } catch (Exception e){
            log.error(TestRailConstants.LOG_ATTACHMENT_ERROR_RESPONSE, resultId, e.getMessage(), e);
        }
    }

    @Override
    public Integer createNewTestRailRun(final List<Integer> caseIds) {
        String url = testRails.getUrl() + TestRailConstants.CREATE_NEW_TEST_RUN_URL + testRails.getProjectId();
        Run request = TestRailUtil.buildTestRunRequest(testRails, caseIds);
        HttpHeaders headers = buildHeaders();
        HttpEntity<Run> entity = new HttpEntity<>(request, headers);
        try {
            log.info(TestRailConstants.LOG_CREATING_TEST_RUN, testRails.getDefaultRunName(), caseIds.size());
            var response = restTemplate.exchange(url, HttpMethod.POST, entity, Run.class);
            Run body = response.getBody();
            if (body != null && body.getId() != null) {
                Integer id = body.getId();
                log.info(TestRailConstants.LOG_TEST_RUN_CREATED, testRails.getDefaultRunName(), id);
                return id;
            }
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_TEST_RUN_CREATION_FAILED, testRails.getDefaultRunName(), e.getMessage(), e);
        }
        return null;
    }

	@Override
	public void validateConnection() {
		String url = testRails.getUrl() + TestRailConstants.GET_PROJECTS_URL;
		HttpHeaders headers = authHeaders();
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		try {
			log.info(TestRailConstants.LOG_VALIDATE_CONNECTION, url);
			var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				log.info(TestRailConstants.LOG_CONNECTION_SUCCESSFUL);
			} else {
				log.warn(TestRailConstants.LOG_CONNECTION_FAILED, response.getStatusCode());
			}

		} catch (Exception e) {
			log.error(TestRailConstants.LOG_CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	@Override
	public Project getProject(Integer projectId) {
		String url = testRails.getUrl() + TestRailConstants.GET_PROJECT_URL + projectId;
		HttpHeaders headers = authHeaders();
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

		try {
			log.info(TestRailConstants.LOG_GET_PROJECT, projectId, testRails.getUrl());
			var response = restTemplate.exchange(url, HttpMethod.GET, entity, Project.class);
			return response.getBody();
		} catch (Exception e) {
			log.error(TestRailConstants.LOG_GET_PROJECT_FAILED, projectId, testRails.getUrl(), e.getMessage(), e);
		}
		return null;
	}

	@Override
	public Suite getSuite(Integer suiteId) {
		String url = testRails.getUrl() + TestRailConstants.GET_SUITE_URL + suiteId;
		HttpHeaders headers = authHeaders();
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

		try {
			log.info(TestRailConstants.LOG_GET_SUITE, suiteId, testRails.getUrl());
			var response = restTemplate.exchange(url, HttpMethod.GET, entity, Suite.class);
			return response.getBody();
		} catch (Exception e) {
			log.error(TestRailConstants.LOG_GET_SUITE_FAILED, suiteId, testRails.getUrl(), e.getMessage(), e);
		}
		return null;
	}

	@Override
	public Run getRun(Integer runId) {
		String url = testRails.getUrl() + TestRailConstants.GET_RUN_URL + runId;
		HttpHeaders headers = authHeaders();
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

		try {
			log.info(TestRailConstants.LOG_GET_RUN, runId, testRails.getUrl());
			var response = restTemplate.exchange(url, HttpMethod.GET, entity, Run.class);
			return response.getBody();
		} catch (Exception e) {
			log.error(TestRailConstants.LOG_GET_RUN_FAILED, runId, testRails.getUrl(), e.getMessage(), e);
		}
		return null;
	}

	@Override
	public Run updateRun(Integer runId, List<Integer> caseIds) {
		String url = testRails.getUrl() + TestRailConstants.UPDATE_RUN_URL + runId;
		Run request = TestRailUtil.buildTestRunRequest(testRails, caseIds);
		HttpHeaders headers = buildHeaders();
		HttpEntity<Run> entity = new HttpEntity<>(request, headers);
		try {
			log.info(TestRailConstants.LOG_UPDATING_TEST_RUN, runId, caseIds.size());
			var response = restTemplate.exchange(url, HttpMethod.POST, entity, Run.class);
			return response.getBody();
		} catch (Exception e) {
			log.error(TestRailConstants.LOG_TEST_RUN_UPDATING_FAILED, runId, e.getMessage(), e);
		}
		return null;
	}

	@Override
	public List<Run> getRunsByProject(Integer projectId) {
		String url = testRails.getUrl() + TestRailConstants.GET_RUNS_URL + projectId;
		HttpHeaders headers = authHeaders();
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

		try {
			log.info(TestRailConstants.LOG_GET_RUNS, projectId, testRails.getUrl());
			var response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Run>>() {});
			return response.getBody();
		} catch (Exception e) {
			log.error(TestRailConstants.LOG_GET_RUNS_FAILED, projectId, testRails.getUrl(), e.getMessage(), e);
		}
		return List.of();
	}

	private HttpHeaders buildHeaders() {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

	private HttpHeaders authHeaders() {
		HttpHeaders headers = new HttpHeaders();

		String auth = testRails.getUsername() + COLON + testRails.getApiKey();
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
		headers.set(TestRailConstants.HEADER_AUTHORIZATION,
				TestRailConstants.AUTH_BASIC_PREFIX + encodedAuth);

		return headers;
	}

    private boolean screenshotsEnabled() {
        return testRails != null
                    && testRails.isEnabled()
                    && testRails.isAddScreenshotForFailure();
    }
}
