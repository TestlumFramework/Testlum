package com.testlum.testing.framework.report.testrail.api.impl;

import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.api.dto.ResultResponse;
import com.testlum.testing.framework.report.testrail.summary.util.TestRailErrorDescriber;
import com.testlum.testing.framework.report.testrail.api.util.TestRailResponseJsonDeserializer;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.TestRailReports;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class TestRailAttachmentApiClient {

    private final TestRailResponseJsonDeserializer jsonDeserializer;
    private final TestRailErrorDescriber errorDescriber;
    private final TestRailConnectionService connectionService;
    private final RestTemplate restTemplate;

    public TestRailAttachmentApiClient(final GlobalTestConfiguration globalTestConfiguration,
                                       final TestRailResponseJsonDeserializer jsonDeserializer,
                                       final TestRailErrorDescriber errorDescriber,
                                       final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.jsonDeserializer = jsonDeserializer;
        this.errorDescriber = errorDescriber;
        TestRailReports testRails = globalTestConfiguration.getReport().getExtentReports().getTestRailReports();
        this.connectionService = new TestRailConnectionService(testRails, restTemplate);
    }

    /**
     * Attaches the failure screenshot to every failed result of the batch
     * and returns how many attachments reached TestRail.
     */
    public int attachScreenshotsForFailedScenarios(final String responseBody,
                                                   final Map<Integer, String> screenshotOfLastUnsuccessfulStep) {
        List<ResultResponse> resultDTOs = jsonDeserializer.extractResultsDTOs(responseBody);
        return resultDTOs.stream()
                .filter(resultDTO -> resultDTO.getStatusId() == TestRailConstants.STATUS_FAILED)
                .mapToInt(resultDTO -> sendAttachmentForResult(screenshotOfLastUnsuccessfulStep, resultDTO))
                .sum();
    }

    private int sendAttachmentForResult(final Map<Integer, String> screenshotOfLastUnsuccessfulStep,
                                        final ResultResponse resultDTO) {
        try {
            return jsonDeserializer.extractIdFieldFromJson(fetchTest(resultDTO.getTestId()),
                            TestRailConstants.CASE_ID)
                    .filter(caseId -> caseId > 0)
                    .flatMap(caseId -> findAttachmentForCase(screenshotOfLastUnsuccessfulStep, caseId))
                    .map(attachmentEntry -> sendAttachment(resultDTO.getId(), attachmentEntry))
                    .orElse(0);
        } catch (Exception e) {
            logFailure(TestRailConstants.LOG_FETCHING_TEST_ERROR_RESPONSE, resultDTO.getTestId(), e);
            return 0;
        }
    }

    private String fetchTest(final Integer testId) {
        String getTestUrl = connectionService.endpoints().getFetchTestEndpoint(String.valueOf(testId));
        log.debug(TestRailConstants.LOG_FETCHING_TEST, testId);
        ResponseEntity<String> response = restTemplate.exchange(getTestUrl, HttpMethod.GET,
                new HttpEntity<>(connectionService.buildHeaders()), String.class);
        log.debug(TestRailConstants.LOG_FETCHING_TEST_SUCCESS_RESPONSE, testId, response.getBody());
        return response.getBody();
    }

    private void logFailure(final String message, final Object id, final Exception exception) {
        log.error(message, id, errorDescriber.describe(exception));
        log.debug(message, id, exception.getMessage(), exception);
    }

    private Optional<Map.Entry<Integer, String>> findAttachmentForCase(
            final Map<Integer, String> screenshotOfLastUnsuccessfulStep, final Integer caseId) {
        return screenshotOfLastUnsuccessfulStep.entrySet().stream()
                .filter(attachmentEntry -> attachmentEntry.getKey().equals(caseId))
                .findFirst();
    }

    private int sendAttachment(final Integer resultId, final Map.Entry<Integer, String> entry) {
        String addAttachmentUrl = connectionService.endpoints().getAddAttachmentEndpoint(resultId);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = buildAttachmentRequest(entry);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(addAttachmentUrl, requestEntity, String.class);
            log.debug(TestRailConstants.LOG_ATTACHMENT_SUCCESS_RESPONSE, resultId, response.getBody());
            return 1;
        } catch (Exception e) {
            logFailure(TestRailConstants.LOG_ATTACHMENT_ERROR_RESPONSE, resultId, e);
            return 0;
        }
    }

    private HttpEntity<MultiValueMap<String, Object>> buildAttachmentRequest(final Map.Entry<Integer, String> entry) {
        ByteArrayResource resource = getImageAsResource(entry);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(TestRailConstants.ATTACHMENT_KEY, resource);
        log.debug(TestRailConstants.LOG_SENDING_ATTACHMENT, resource);
        return new HttpEntity<>(body, buildAttachmentRequestHeaders());
    }

    private HttpHeaders buildAttachmentRequestHeaders() {
        HttpHeaders httpHeaders = connectionService.buildHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        return httpHeaders;
    }

    private ByteArrayResource getImageAsResource(final Map.Entry<Integer, String> entry) {
        byte[] imageBytes = Base64.getDecoder().decode(entry.getValue());
        return new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return TestRailConstants.ATTACHMENT_DEFAULT_FILENAME;
            }
        };
    }
}
