package com.knubisoft.testlum.testing.framework.scenario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.apache.commons.text.StringEscapeUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonVariationInjectionStrategy implements ScenarioContextVariationInjectionStrategy {

    private static final String RAW_REGEXP = "(\"raw\"\\s*:\\s*\")(?:[^\"\\\\]|\\\\.)*(\")";
    private static final Pattern RAW_PATTERN = Pattern.compile(RAW_REGEXP);
    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);
    private static final String ABSENT_MARKER = "j(absent)";
    private static final String NULL_MARKER = "j(null)";
    private static final String EMPTY_MARKER = "j(empty)";
    private static final String EXISTS_MARKER = "j(exists)";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode currentRawJson;


    @Override
    public boolean isApplicable(String scenarioStepAsString) {
        boolean isApplicableStrategy = false;
        try {
            JsonNode jsonNode = objectMapper.readValue(scenarioStepAsString, JsonNode.class);
            List<String> methodsWithPayload = List.of("post", "put", "patch");
            for (String method : methodsWithPayload) {
                if (jsonNode.get(method) != null) {
                    JsonNode rawJsonNode = jsonNode.get(method).get("body").get("raw");
                    isApplicableStrategy =
                            checkIfPlaceholdersExistsInJsonBody(rawJsonNode);
                    break;
                }
            }
        } catch (Exception e) {
            return isApplicableStrategy;
        }
        //get raw from scenario step as string and check variations here
        return isApplicableStrategy;
    }

    private boolean checkIfPlaceholdersExistsInJsonBody(JsonNode rawJsonNode) {
        Matcher m = ROUTE_PATTERN.matcher(rawJsonNode.asText());
        if (m.find()) {
            currentRawJson = rawJsonNode;
            return true;
        }
        return false;
    }

    @SneakyThrows
    @Override
    public String inject(String scenarioStepAsString, ScenarioContext scenarioContext) {
        Map<String, String> parentMarkers = collectMarkedJsonPairs(scenarioContext.getContextMap());
        String currentRawJsonText = currentRawJson.asText();
        ObjectNode jsonNode = ((ObjectNode) objectMapper.readTree(currentRawJsonText));
        JsonNode preProcessedJsonNode = preProcessJsonForParentsNegativeKeyWords(jsonNode, parentMarkers);
        String ejectedJson = objectMapper.writeValueAsString(preProcessedJsonNode);
        Matcher m = ROUTE_PATTERN.matcher(ejectedJson);
        while (m.find()) {
            String scenarioPlaceholder = m.group(0);
            String csvColumnName = m.group(1);
            String valueForPlaceholder = scenarioContext.get(csvColumnName);
            String path = csvColumnName.trim().substring(2, csvColumnName.length() - 1);
            int lastPathDelimiter = path.lastIndexOf(".");
            String parent = null;
            if (lastPathDelimiter > 0) {
                parent = "j(" + path.substring(0, lastPathDelimiter) + ")";
            }
            String parentMarkerForParticularColumn = parentMarkers.get(parent);
            if (parentMarkerForParticularColumn == null || parentMarkerForParticularColumn.contains(EXISTS_MARKER)) {
                valueForPlaceholder = resolveNestedPlaceholders(valueForPlaceholder, scenarioContext);
                ejectedJson = ejectedJson.replace(scenarioPlaceholder, valueForPlaceholder);

            }
        }
        String bodyReplacementJson = StringEscapeUtils.escapeJson(ejectedJson);
        Matcher matcher = RAW_PATTERN.matcher(scenarioStepAsString);
        return matcher.replaceFirst("$1" + Matcher.quoteReplacement(bodyReplacementJson) + "$2");
    }

    private JsonNode preProcessJsonForParentsNegativeKeyWords(ObjectNode jsonNode, Map<String, String> parentMarkers) {
        for (Map.Entry<String, String> entry : parentMarkers.entrySet()) {
            String path = entry.getKey();
            String value = entry.getValue();
            if (value.contains(NULL_MARKER)) {
                setNodeValue(jsonNode, path, null);
            } else if (value.contains(EMPTY_MARKER)) {
                if (isArrayPath(path)){
                    setNodeValue(jsonNode, path, objectMapper.createArrayNode());
                } else {
                    setNodeValue(jsonNode, path, objectMapper.createObjectNode());
                }
            } else if (value.contains(ABSENT_MARKER)) {
                removeNode(jsonNode, path);
            }
        }
        return jsonNode;
    }

    private boolean isArrayPath(String path) {
        return path.endsWith("[])");
    }


    private void setNodeValue(JsonNode processedJson, String path, JsonNode valueToSet) {
        String cleanPath = path.trim().substring(2, path.length() - 1);
        String[] jsonParts = cleanPath.split("\\.");
        ObjectNode current = (ObjectNode) processedJson;
        for (int i = 0; i < jsonParts.length - 1; i++) {
            String part = jsonParts[i];
            if (part.endsWith("[]")) {
                part = part.substring(0, part.length() - 2);
            }
            current = (ObjectNode) current.get(part);
        }
        String lastKey = jsonParts[jsonParts.length - 1].trim();
        if (lastKey.endsWith("[]")) {
            lastKey = lastKey.substring(0, lastKey.length() - 2);
        }
        if (valueToSet == null) {
            current.putNull(lastKey);
        } else {
            current.set(lastKey, valueToSet);
        }
    }

    private void removeNode(JsonNode processedJson, String path) {
        String cleanPath = path.trim().substring(2, path.length() - 1);
        String[] jsonParts = cleanPath.split("\\.");
        ObjectNode current = (ObjectNode) processedJson;
        for (int i = 0; i < jsonParts.length - 1; i++) {
            String part = jsonParts[i];
            if (part.endsWith("[]")) {
                part = part.substring(0, part.length() - 2);
            }
            current = (ObjectNode) current.get(part);
        }
        String lastKey = jsonParts[jsonParts.length - 1];
        if (lastKey.endsWith("[]")) {
            lastKey = lastKey.substring(0, lastKey.length() - 2);
        }
        current.remove(lastKey);
    }

    private String resolveNestedPlaceholders(String valueForPlaceholder, ScenarioContext scenarioContext) {
        Matcher nestedMatcher = ROUTE_PATTERN.matcher(valueForPlaceholder);
        String replaced = valueForPlaceholder;
        while (nestedMatcher.find()) {
            String nestedPlaceholder = nestedMatcher.group(0);
            if (!nestedPlaceholder.contains("j(")) {
                String nestedKey = nestedMatcher.group(1);
                String nestedValue = scenarioContext.get(nestedKey);
                replaced = replaced.replace(nestedPlaceholder, nestedValue);
            }
        }
        return replaced;
    }

    private static Map<String, String> collectMarkedJsonPairs(Map<String, String> result) {
        Map<String, String> parentMarkers = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : result.entrySet()) {
            String path = entry.getKey();
            String value = entry.getValue();
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
}
