package com.testlum.testing.framework.report.testrail.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.model.ResultResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class TestRailResponseJsonDeserializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public List<ResultResponseDto> extractResultsDTOs(final String jsonResponse) {
        List<ResultResponseDto> results = new ArrayList<>();
        try {
            JsonNode root = OBJECT_MAPPER.readTree(jsonResponse);
            if (root.isArray()) {
                for (JsonNode resultNode : root) {
                    ResultResponseDto resultDto = mapNodeToResult(resultNode);
                    results.add(resultDto);
                }
            }
        } catch (Exception e) {
            throw new DefaultFrameworkException(ExceptionMessage.ERROR_ON_PARSING_JSON, e);
        }
        return results;
    }

    public Optional<Integer> extractIdFieldFromJson(final String jsonResponse, final String idField) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(jsonResponse);
            if (!root.isArray()) {
                JsonNode idNode = root.get(idField);
                if (idNode != null && idNode.isInt()) {
                    return Optional.of(idNode.asInt());
                }
            }
        } catch (Exception e) {
            log.error(TestRailConstants.ID_FETCH_ERROR_RESPONSE, idField);
            return Optional.empty();
        }
        return Optional.empty();
    }

    private ResultResponseDto mapNodeToResult(final JsonNode resultNode) {
        return ResultResponseDto.builder()
                .id(resultNode.get(TestRailConstants.ID_FIELD).asInt())
                .testId(resultNode.get(TestRailConstants.TEST_ID).asInt())
                .statusId(resultNode.get(TestRailConstants.STATUS_ID).asInt())
                .build();
    }
}
