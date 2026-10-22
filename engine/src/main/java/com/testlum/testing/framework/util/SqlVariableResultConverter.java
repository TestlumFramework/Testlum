package com.testlum.testing.framework.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SqlVariableResultConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SqlVariableResultConverter() {
    }

    public static String convert(final List<? extends Map<String, ?>> rows) {
        if (rows.isEmpty() || rows.get(0).isEmpty()) {
            throw new DefaultFrameworkException(ExceptionMessage.VAR_QUERY_RESULT_ERROR);
        }
        if (rows.size() == 1 && rows.get(0).size() == 1) {
            return toText(rows.get(0).values().iterator().next());
        }
        return toJson(rows);
    }

    private static String toJson(final List<? extends Map<String, ?>> rows) {
        List<Map<String, Object>> normalizedRows = new ArrayList<>();
        for (Map<String, ?> row : rows) {
            Map<String, Object> normalizedRow = new LinkedHashMap<>();
            row.forEach((column, value) -> normalizedRow.put(column, normalize(value)));
            normalizedRows.add(normalizedRow);
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(normalizedRows);
        } catch (JsonProcessingException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private static Object normalize(final Object value) {
        if (value == null || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        return toText(value);
    }

    private static String toText(final Object value) {
        if (value instanceof byte[] bytes) {
            return Base64.getEncoder().encodeToString(bytes);
        }
        return String.valueOf(value);
    }
}
