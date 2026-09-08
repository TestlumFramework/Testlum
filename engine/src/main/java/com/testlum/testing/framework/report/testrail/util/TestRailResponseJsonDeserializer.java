package com.testlum.testing.framework.report.testrail.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.model.ResultResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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

    /**
     * Collects customKeyValue - caseId pairs from one page of the
     * get_cases response and returns how many cases that page held.
     */
    public int collectCaseIdsByMatchKey(final String jsonResponse, final String caseMatchKey,
                                        final Map<String, Integer> target) {
        JsonNode root;
        try {
            root = OBJECT_MAPPER.readTree(jsonResponse);
        } catch (JsonProcessingException e) {
            throw new DefaultFrameworkException(ExceptionMessage.ERROR_ON_PARSING_JSON, e);
        }
        JsonNode cases = root.isArray() ? root : root.get(TestRailConstants.CASES_FIELD);
        if (cases == null || !cases.isArray()) {
            return 0;
        }
        cases.forEach(caseNode -> collectCase(caseNode, caseMatchKey, target));
        return cases.size();
    }

    private void collectCase(final JsonNode caseNode, final String caseMatchKey,
                             final Map<String, Integer> target) {
        Integer caseId = readInt(caseNode, TestRailConstants.ID_FIELD);
        String matchKeyValue = readMatchKeyValue(caseNode, caseMatchKey);
        if (caseId == null || StringUtils.isBlank(matchKeyValue)) {
            return;
        }
        Integer previous = target.putIfAbsent(matchKeyValue, caseId);
        if (previous != null && !previous.equals(caseId)) {
            log.error(TestRailConstants.CASE_MATCH_KEY_VALUE_DUPLICATED,
                    caseMatchKey, matchKeyValue, previous, caseId, previous);
        }
    }

    private static String readMatchKeyValue(final JsonNode caseNode, final String caseMatchKey) {
        JsonNode matchKeyNode = caseNode.get(caseMatchKey);
        if (matchKeyNode == null || matchKeyNode.isNull()) {
            return null;
        }
        return matchKeyNode.isTextual() ? matchKeyNode.textValue() : matchKeyNode.asText();
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
