package com.testlum.testing.framework.report.testrail.util;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.stream.StreamSupport;

@Component
@Slf4j
public class TestRailResponseJsonDeserializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public List<ResultResponseDto> extractResultsDTOs(final String jsonResponse) {
        JsonNode root;
        try {
            root = OBJECT_MAPPER.readTree(jsonResponse);
        } catch (JsonProcessingException e) {
            throw new DefaultFrameworkException(ExceptionMessage.ERROR_ON_PARSING_JSON, e);
        }
        if (!root.isArray()) {
           return List.of();
        }
        return StreamSupport.stream(root.spliterator(), false)
                .map(this::mapNodeToResult)
                .flatMap(Optional::stream)
                .toList();
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

    private Optional<ResultResponseDto> mapNodeToResult(final JsonNode resultNode) {
        final Integer id = readInt(resultNode, TestRailConstants.ID_FIELD);
        final Integer testId = readInt(resultNode, TestRailConstants.TEST_ID);
        final Integer statusId = readInt(resultNode, TestRailConstants.STATUS_ID);
        if (id == null || testId == null || statusId == null) {
            return Optional.empty();
        }
        return Optional.of(ResultResponseDto.builder()
                .id(id)
                .testId(testId)
                .statusId(statusId)
                .build());
    }

    private static Integer readInt(final JsonNode node, final String fieldName) {
        final JsonNode field = node.get(fieldName);
        return field != null && field.isInt() ? field.intValue() : null;
    }
}
