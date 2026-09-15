package com.testlum.testing.framework.report.testrail.impl;

import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.model.ResultResponseDto;
import com.testlum.testing.framework.report.testrail.util.TestRailResponseJsonDeserializer;
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
    private final TestRailConnectionService connectionService;
    private final RestTemplate restTemplate;

    public TestRailAttachmentApiClient(final GlobalTestConfiguration globalTestConfiguration,
                                       final TestRailResponseJsonDeserializer jsonDeserializer,
                                       final RestTemplate restTemplate) {
        TestRailReports testRails = globalTestConfiguration.getReport().getExtentReports().getTestRailReports();
        this.restTemplate = restTemplate;
        this.jsonDeserializer = jsonDeserializer;
        this.connectionService = new TestRailConnectionService(testRails, restTemplate);
    }


    public void attachScreenshotsForFailedScenarios(final String responseBody,
                                                    final Map<Integer, String> screenshotOfLastUnsuccessfulStep) {
        List<ResultResponseDto> resultDTOs = jsonDeserializer.extractResultsDTOs(responseBody);
        resultDTOs.stream()
                .filter(resultDTO -> resultDTO.getStatusId() == TestRailConstants.STATUS_FAILED)
                .forEach(resultDTO -> sendAttachmentForResult(screenshotOfLastUnsuccessfulStep, resultDTO));
    }

    private void sendAttachmentForResult(final Map<Integer, String> screenshotOfLastUnsuccessfulStep,
                                         final ResultResponseDto resultDTO) {
        String getTestUrl = connectionService.endpoints().getFetchTestEndpoint(String.valueOf(resultDTO.getTestId()));
        try {
            log.info(TestRailConstants.LOG_FETCHING_TEST, resultDTO.getTestId());
            ResponseEntity<String> response = restTemplate.exchange(getTestUrl, HttpMethod.GET,
                    new HttpEntity<>(connectionService.buildHeaders()), String.class);
            log.info(TestRailConstants.LOG_FETCHING_TEST_SUCCESS_RESPONSE, resultDTO.getTestId(), response.getBody());
            jsonDeserializer.extractIdFieldFromJson(response.getBody(), TestRailConstants.CASE_ID)
                    .filter(caseId -> caseId > 0)
                    .flatMap(caseId -> findAttachmentForCase(screenshotOfLastUnsuccessfulStep, caseId))
                    .ifPresent(attachmentEntry -> sendAttachment(resultDTO.getId(), attachmentEntry));
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_FETCHING_TEST_ERROR_RESPONSE, resultDTO.getTestId(), e.getMessage(), e);
        }
    }

    private Optional<Map.Entry<Integer, String>> findAttachmentForCase(
            final Map<Integer, String> screenshotOfLastUnsuccessfulStep, final Integer caseId) {
        return screenshotOfLastUnsuccessfulStep.entrySet().stream()
                .filter(attachmentEntry -> attachmentEntry.getKey().equals(caseId))
                .findFirst();
    }

    private void sendAttachment(final Integer resultId, final Map.Entry<Integer, String> entry) {
        String addAttachmentUrl = connectionService.endpoints().getAddAttachmentEndpoint(resultId);
        HttpHeaders httpHeaders = buildAttachmentRequestHeaders();
        ByteArrayResource resource = getImageAsResource(entry);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(TestRailConstants.ATTACHMENT_KEY, resource);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);
        try {
            log.info(TestRailConstants.LOG_SENDING_ATTACHMENT, resource);
            ResponseEntity<String> response = restTemplate.postForEntity(addAttachmentUrl, requestEntity, String.class);
            log.info(TestRailConstants.LOG_ATTACHMENT_SUCCESS_RESPONSE, resultId, response.getBody());
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_ATTACHMENT_ERROR_RESPONSE, resultId, e.getMessage(), e);
        }
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
