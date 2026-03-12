package com.knubisoft.testlum.testing.framework.util;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Old replace strategy. Kept for now
 *
 */
@Deprecated
public class JsonSpecialMarkingsParser {

    private static final String ABSENT_MARKER = "j(absent)";
    private static final String NULL_MARKER = "j(null)";
    private static final String EMPTY_MARKER = "j(empty)";
    private static final String EXISTS_MARKER = "j(exists)";


    public static String buildJson(Map<String, String> contextMap) {
        Map<String, Object> result = removeFromContextMapKeysSpecialJsonMarks(contextMap);
        DocumentContext ctx = JsonPath.parse("{}");
        Map<String, Object> parentMarkers = collectMarkedJsonPairs(result);

        for (Map.Entry<String, Object> entry : result.entrySet()) {
            processEntry(entry.getKey(), String.valueOf(entry.getValue()), ctx, parentMarkers);
        }
        String json = ctx.jsonString();
        return StringEscapeUtils.escapeJson(json);
    }

    private static Map<String, Object> removeFromContextMapKeysSpecialJsonMarks(Map<String, String> contextMap) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> stringObjectEntry : contextMap.entrySet()) {
            if (!stringObjectEntry.getKey().contains("expected")) {
                String csvColumnName = stringObjectEntry.getKey().trim();
                String csvColumnNameSanitized = csvColumnName.substring(2, csvColumnName.length() - 1);
                result.put(csvColumnNameSanitized, stringObjectEntry.getValue());
            }
        }
        return result;
    }

    private static Map<String, Object> collectMarkedJsonPairs(Map<String, Object> result) {
        Map<String, Object> parentMarkers = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            String path = entry.getKey();
            String value = String.valueOf(entry.getValue());
            if (isMarker(value)) {
                parentMarkers.put(path, value);
            }
        }
        return parentMarkers;
    }

    private static boolean isMarker(String value) {
        return value.startsWith(EXISTS_MARKER)
                || value.startsWith(NULL_MARKER)
                || value.startsWith(EMPTY_MARKER)
                || value.startsWith(ABSENT_MARKER);
    }

    private static void processEntry(String path, String pathValue, DocumentContext ctx, Map<String, Object> markers) {
        String[] jsonParts = path.split("\\.");
        if (isMarker(pathValue)) {
            handleMarker(path, pathValue, jsonParts, ctx, markers);
        } else if (jsonParts.length > 1) {
            handleNested(jsonParts, ctx, pathValue, markers);
        } else {
            ctx.put("$", path, pathValue);
        }
    }

    public static void handleMarker(String path, String valueWithMarker, String[] jsonParts, DocumentContext ctx, Map<String, Object> markers) {
        if (valueWithMarker.contains(ABSENT_MARKER)) {
            return;
        }
        Object marker = getAppropriateMarkerValue(valueWithMarker);
        boolean simpleJsonPair = jsonParts.length == 1;
        if (simpleJsonPair) {
            ctx.put("$", path, marker);
        } else {
            String parentPath = buildParentPathForExistsKeyword(markers, jsonParts, ctx);
            if (parentPath == null) {
                return;
            }
            int lastPart = jsonParts.length - 1;
            String childKey = jsonParts[lastPart];
            ctx.put("$." + parentPath, childKey, marker);
        }
    }


    private static Object getAppropriateMarkerValue(String valueWithMarker) {
        if (valueWithMarker.contains(NULL_MARKER)) {
            return null;
        }
        return new LinkedHashMap<>();
    }


    /**
     * loop through length - 2 : a.b.c.d.e.f -> a, a.b, a.b.c, a.b.c.d, a.b.c.d.e
     *
     * @param ctx
     * @param value
     * @param parentMarkers
     */
    private static void handleNested(String[] jsonParts, DocumentContext ctx, String value, Map<String, Object> parentMarkers) {
        String parentPath = buildParentPathForExistsKeyword(parentMarkers, jsonParts, ctx);
        if (parentPath == null) {
            return;
        }
        String childKey = jsonParts[jsonParts.length - 1];
        setValueForChildJsonEntry(ctx, parentPath, childKey, value);
    }

    private static void setValueForChildJsonEntry(DocumentContext ctx, String parentPath, String childKey, String value) {
        ctx.put("$." + parentPath, childKey, value);
    }

    private static String buildParentPathForExistsKeyword(Map<String, Object> parentMarkers, String[] jsonPaths, DocumentContext ctx) {
        StringBuilder builtPath = new StringBuilder();
        for (int i = 0; i < jsonPaths.length - 1; i++) {
            if (i > 0) {
                builtPath.append(".");
            }
            builtPath.append(jsonPaths[i]);
            String marker = String.valueOf(parentMarkers.get(builtPath.toString()));
            if (marker != null && isAvoidingBuildingPathMarker(marker)) {
                return null;
            }
        }
        return builtPath.toString();
    }


    private static boolean isAvoidingBuildingPathMarker(String marker) {
        return marker.contains(NULL_MARKER)
                || marker.contains(EMPTY_MARKER)
                || marker.contains(ABSENT_MARKER);
    }
}
