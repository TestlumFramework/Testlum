package com.knubisoft.testlum.testing.framework.scenario;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.knubisoft.testlum.testing.framework.scenario.jsonInjection.PlaceholderNormalizer;
import lombok.SneakyThrows;
import org.apache.commons.text.StringEscapeUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Strategy for injecting variation values directly into JSON body placeholders.
 *
 * <p>This strategy handles HTTP request bodies (POST, PUT, PATCH) containing JSON with
 * placeholder syntax {@code {{j(path)}}} where values from CSV variations are injected.</p>
 *
 * <h2>Placeholder Syntax</h2>
 * <ul>
 *   <li>{@code "field": "{{j(fieldName)}}"} - quoted placeholder for string values</li>
 *   <li>{@code "field": {{j(fieldName)}}} - unquoted placeholder for non-string values (numbers, booleans, arrays)</li>
 *   <li>{@code "items": [{{j(items[])}}]} - array placeholder for injecting array elements</li>
 * </ul>
 *
 * <h2>Parent Markers</h2>
 * <p>Special markers control parent node behavior in the JSON structure:</p>
 * <ul>
 *   <li>{@code j(exists)} - parent node exists normally, children are injected</li>
 *   <li>{@code j(null)} - parent node is set to JSON null</li>
 *   <li>{@code j(empty)} - parent node is set to empty object {@code {}} or empty array {@code []}</li>
 *   <li>{@code j(absent)} - parent node is completely removed from the JSON</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>
 * JSON template:
 * {"user": {"name": "{{j(user.name)}}", "age": {{j(user.age)}}}, "tags": [{{j(tags[])}}]}
 *
 * CSV variations:
 * j(user)      | j(user.name) | j(user.age) | j(tags[])
 * j(exists)    | John         | 25          | "a","b","c"
 * j(null)      | -            | -           | -
 * j(absent)    | -            | -           | -
 * </pre>
 *
 * @see ScenarioContextVariationInjectionStrategy
 */
public class JsonVariationInjectionStrategy implements ScenarioContextVariationInjectionStrategy {

    /** Regex pattern to match the "raw" field in scenario step JSON. */
    private static final String RAW_NODE_IN_BODY_REGEXP = "(\"raw\"\\s*:\\s*\")(?:[^\"\\\\]|\\\\.)*(\")";
    private static final Pattern RAW_PATTERN = Pattern.compile(RAW_NODE_IN_BODY_REGEXP);

    /** Regex pattern to match placeholders in format {{...}}. */
    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);

    /** Marker indicating the parent node should be removed from JSON. */
    private static final String ABSENT_MARKER = "$absent";

    /** Marker indicating the parent node should be set to null. */
    private static final String NULL_MARKER = "$null";

    /** Marker indicating the parent node should be an empty object or array. */
    private static final String EMPTY_MARKER = "$empty";

    /** Marker indicating the parent node exists and children should be injected normally. */
    private static final String EXISTS_MARKER = "$exists";

    private static final PlaceholderNormalizer placeholderNormalizer = new PlaceholderNormalizer();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Cached reference to the raw JSON node from the scenario step. */
    private JsonNode currentRawJson;


    /**
     * Determines if this strategy is applicable to the given scenario step.
     *
     * <p>This strategy applies when the scenario step contains a POST, PUT, or PATCH
     * HTTP method with a body containing JSON placeholders in {@code {{...}}} format.</p>
     *
     * @param scenarioStepAsString the scenario step JSON as a string
     * @return {@code true} if the step contains a supported HTTP method with JSON placeholders,
     *         {@code false} otherwise
     */
    @Override
    public boolean isApplicable(String scenarioStepAsString) {
        boolean isApplicableStrategy = false;
        try {
            JsonNode jsonNode = objectMapper.readValue(scenarioStepAsString, JsonNode.class);
            List<String> methodsWithPayload = List.of("post", "put", "patch");
            for (String method : methodsWithPayload) {
                if (jsonNode.get(method) != null) {
                    JsonNode jsonBodyNode = jsonNode.get(method).get("body");
                    if (jsonBodyNode == null) {
                        continue;
                    }
                    JsonNode payloadJsonNode = jsonBodyNode.get("raw");
                    isApplicableStrategy = checkIfPlaceholdersExistsInJsonBody(payloadJsonNode);
                    return isApplicableStrategy;
                }
            }
        } catch (Exception e) {
            return isApplicableStrategy;
        }
        return isApplicableStrategy;
    }

    /**
     * Checks if the raw JSON body contains any placeholders.
     *
     * <p>If placeholders are found, caches the raw JSON node for later injection.</p>
     *
     * @param rawJsonNode the raw JSON node from the request body
     * @return {@code true} if placeholders exist, {@code false} otherwise
     */
    private boolean checkIfPlaceholdersExistsInJsonBody(JsonNode rawJsonNode) {
        Matcher m = ROUTE_PATTERN.matcher(rawJsonNode.asText());
        if (m.find()) {
            currentRawJson = rawJsonNode;
            return true;
        }
        return false;
    }

    /**
     * Injects variation values into the JSON body placeholders.
     *
     * <p>The injection process:</p>
     * <ol>
     *   <li>Collects parent markers (absent, null, empty, exists) from the context</li>
     *   <li>Pre-processes the JSON to apply parent marker transformations</li>
     *   <li>Replaces remaining placeholders with actual values from the scenario context</li>
     *   <li>Resolves any nested placeholders within values</li>
     *   <li>Escapes the final JSON and replaces the raw body in the scenario step</li>
     * </ol>
     *
     * @param scenarioStepAsString the original scenario step JSON string
     * @param scenarioContext      the context containing variation values from CSV
     * @param escapeSpelQuotes
     * @return the scenario step with all placeholders replaced by actual values
     */
    @SneakyThrows
    @Override
    public String injectVariationsValues(String scenarioStepAsString, ScenarioContext scenarioContext, boolean escapeSpelQuotes) {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        Map<String, String> parentMarkers = collectMarkedJsonPairs(scenarioContext.getContextMap());
        String currentRawJsonText = currentRawJson.asText();
        String jsonWithRawPlaceholdersIfAny =
                placeholderNormalizer.replacePlaceholdersWithoutQuotesWithRawMark(currentRawJsonText);
        JsonNode root = objectMapper.readTree(jsonWithRawPlaceholdersIfAny);
        JsonNode preProcessedJsonNode;
        if (root.isObject()) {
            preProcessedJsonNode = preProcessJsonForParentsNegativeKeyWords((ObjectNode) root, parentMarkers);
        } else if (root instanceof ArrayNode) {
            preProcessedJsonNode = preProcessRootArray(root, parentMarkers);
        } else {
            preProcessedJsonNode = root;
        }
        String ejectedJson = objectMapper.writeValueAsString(preProcessedJsonNode);
        Matcher m = ROUTE_PATTERN.matcher(ejectedJson);
        while (m.find()) {
            String scenarioPlaceholder = m.group(0); // {{j(tagsMinItems[])}}
            String csvColumnName = m.group(1); //j(tagsMinItems[])
            String valueForPlaceholder = scenarioContext.get(csvColumnName); // "Ve"
            int lastPathDelimiter = csvColumnName.lastIndexOf(".");
            String parent = null;
            if (lastPathDelimiter > 0) {
                parent = csvColumnName.substring(0, lastPathDelimiter);
            }
            String parentMarkerForParticularColumn = parentMarkers.get(parent);
            if (parentMarkerForParticularColumn == null || parentMarkerForParticularColumn.contains(EXISTS_MARKER)) {
                valueForPlaceholder = resolveNestedPlaceholders(valueForPlaceholder, scenarioContext);
                ejectedJson =  placeholderNormalizer.denormalize(scenarioPlaceholder, ejectedJson, valueForPlaceholder);
            }
        }
        String bodyReplacementJson = StringEscapeUtils.escapeJson(ejectedJson);
        Matcher matcher = RAW_PATTERN.matcher(scenarioStepAsString);
        return matcher.replaceFirst("$1" + Matcher.quoteReplacement(bodyReplacementJson) + "$2");
    }

    /**
     * Pre-processes the JSON tree to apply parent marker transformations.
     *
     * <p>Handles the following markers:</p>
     * <ul>
     *   <li>{@code j(null)} - sets the node to JSON null</li>
     *   <li>{@code j(empty)} - sets the node to empty array or object based on path</li>
     *   <li>{@code j(absent)} - removes the node entirely from the JSON</li>
     * </ul>
     *
     * @param jsonNode the JSON object node to process
     * @param parentMarkers map of JSON paths to their marker values
     * @return the processed JSON node with markers applied
     */
    private JsonNode preProcessJsonForParentsNegativeKeyWords(ObjectNode jsonNode, Map<String, String> parentMarkers) {
        for (Map.Entry<String, String> entry : parentMarkers.entrySet()) {
            String path = entry.getKey();
            String value = entry.getValue();
            if (value.contains(NULL_MARKER)) {
                if (isIndexedArrayPath(path)){
                    setArrayElementToNull(jsonNode, path);
                } else if (isArrayPath(path)) {
                    setNodeValue(jsonNode, path, null, true);
                } else {
                    setNodeValue(jsonNode, path, null, false);
                }
            } else if (value.contains(EMPTY_MARKER)) {
                if (isArrayPath(path)){
                    setNodeValue(jsonNode, path, objectMapper.createArrayNode(), true);
                } else if (isIndexedArrayPath(path)){
                    setArrayElementToEmptyValue(jsonNode, path);
                } else {
                    setNodeValue(jsonNode, path, objectMapper.createObjectNode(), false);
                }
            } else if (value.contains(ABSENT_MARKER)) {
                if (isIndexedArrayPath(path)){
                    removeArrayElement(jsonNode, path);
                } else {
                    removeNode(jsonNode, path);
                }
            }
        }
        return jsonNode;
    }

    private JsonNode preProcessRootArray(JsonNode root, Map<String, String> parentMarkers) {
        for (String value : parentMarkers.values()) {
            if (value.contains(NULL_MARKER)) {
                return JsonNodeFactory.instance.nullNode();
            } else if (value.contains(EMPTY_MARKER)) {
                return objectMapper.createArrayNode();
            }
        }
        return root;
    }

    private void removeArrayElement(ObjectNode jsonNode, String path) {
        ArrayNodeWithIndexHolder arrayNodeWithIndexHolder = navigateToArrayElement(jsonNode, path);
        if (arrayNodeWithIndexHolder != null) {
            arrayNodeWithIndexHolder.arrayNode.remove(arrayNodeWithIndexHolder.index);
        }
    }

    private void setArrayElementToEmptyValue(ObjectNode jsonNode, String path) {
        ArrayNodeWithIndexHolder arrayNodeWithIndexHolder = navigateToArrayElement(jsonNode, path);
        if (arrayNodeWithIndexHolder != null) {
            int nodeIndex = arrayNodeWithIndexHolder.index;
            JsonNode currentNodeByIndex = arrayNodeWithIndexHolder.arrayNode.get(nodeIndex);
            JsonNode nodeToSet =  currentNodeByIndex.isArray()
                    ? objectMapper.createArrayNode()
                    : objectMapper.createObjectNode();
            arrayNodeWithIndexHolder.arrayNode.set(nodeIndex, nodeToSet);
        }
    }



    private void setArrayElementToNull(ObjectNode jsonNode, String path) {
        ArrayNodeWithIndexHolder arrayNodeWithIndexHolder = navigateToArrayElement(jsonNode, path);
        if (arrayNodeWithIndexHolder != null) {
            arrayNodeWithIndexHolder.arrayNode.set(arrayNodeWithIndexHolder.index, JsonNodeFactory.instance.nullNode());
        }
    }

    /**
     * Determines if the given path represents an array field.
     *
     * @param path the JSON path in format {@code j(path[])}
     * @return {@code true} if the path ends with {@code [])}, indicating an array
     */
    private boolean isArrayPath(String path) {
        return path.endsWith("[]");
    }

    /**
     * Determines if the given path represents an array field with index [\\d]
     *
     * @param path the JSON path in format {@code j(path[5])}
     * @return {@code true} if the path ends with {@code [5])}, indicating an array with index
     */
    private boolean isIndexedArrayPath(String path) {
        return path.matches(".*\\[\\d+]$");
    }

     /**
     *  Navigates through nested JSON structure to find the target array and element index.
     *  Handles paths like: items[0], items[0].additionalInfo[2], nested.deep.array[5]
     *
     *  @param jsonNode the root JSON node
     *  @param path the full path ending with an indexed array element
     *  @return ArrayNodeWithIndex containing the target array and index, or null if not found
     *
     * */
    private ArrayNodeWithIndexHolder navigateToArrayElement(JsonNode jsonNode, String path) {
        String cleanPath = path.trim();
        String[] jsonPars = cleanPath.split("\\.");
        JsonNode currentArray = jsonNode;
        for (int i = 0; i < jsonPars.length - 1; i++) {
            String part = jsonPars[i];
            if (part.endsWith("[]")) {
                part = part.substring(0, part.length() - 2);
                currentArray = currentArray.get(part);
            } else if (part.matches(".*\\[\\d+]")) {
                String arrayName = part.replaceAll("\\[\\d+]$", "");
                int elementFromArrayIndex = Integer.parseInt(part.replaceAll(".*\\[(\\d+)]$", "$1"));
                currentArray = currentArray.get(arrayName);
                if (currentArray == null) {
                    return null;
                }
                currentArray = currentArray.get(elementFromArrayIndex);
            } else {
                currentArray = currentArray.get(part);
            }
        }
        String lastPath = jsonPars[jsonPars.length - 1];
        if (lastPath.matches(".*\\[\\d+]$")) {
            String arrayName = lastPath.replaceAll("\\[\\d+]$", "");
            int elementIndex = Integer.parseInt(lastPath.replaceAll(".*\\[(\\d+)]$", "$1"));
            JsonNode arrayNode = currentArray.get(arrayName);
            if (arrayNode instanceof ArrayNode node) {
                return new ArrayNodeWithIndexHolder(elementIndex, node);
            }
        }
        return null;
    }


    private void setNodeValue(JsonNode processedJson, String path, JsonNode valueToSet, boolean isManipulatingArray) {
        String cleanPath = path.trim();
        String[] jsonParts = cleanPath.split("\\.");
        JsonNode current = processedJson;

        for (int i = 0; i < jsonParts.length - 1; i++) {
            String part = jsonParts[i];
            if (part.endsWith("[]")) {
                // Array parent: items[] -> items
                part = part.substring(0, part.length() - 2);
                current = current.get(part);
            } else if (part.matches(".*\\[\\d+]")) {
                // Indexed: items[0] -> get "items" array, then element at index 0
                String arrayName = part.replaceAll("\\[\\d+]$", "");
                int index = Integer.parseInt(part.replaceAll(".*\\[(\\d+)]$", "$1"));
                current = current.get(arrayName).get(index);
            } else {
                // Regular field
                current = current.get(part);
            }
        }
        // Now current points to the parent object, set the last key
        ObjectNode currentObj = (ObjectNode) current;
        String lastKey = jsonParts[jsonParts.length - 1].trim();
        if (isManipulatingArray) {
            if (lastKey.endsWith("[]")) {
                lastKey = lastKey.substring(0, lastKey.length() - 2); //subsctring for the node key
            } else if (lastKey.matches("^\\w+\\[\\d+]$")) {
                String nodeKey = lastKey.replaceAll("\\[\\d+]", "");
                int currentElementIndex = Integer.parseInt(lastKey.replaceAll("[a-zA-Z\\[\\]]", ""));
                ArrayNode arrayNode = (ArrayNode) current.get(nodeKey);
                if (valueToSet == null) {
                    arrayNode.setNull(currentElementIndex);
                } else {
                    arrayNode.remove(currentElementIndex);
                    arrayNode.set(currentElementIndex, valueToSet);
                }
                currentObj.set(nodeKey, arrayNode);
                return;
            }
        }
        if (valueToSet == null) {
            currentObj.putNull(lastKey);
        } else {
            currentObj.set(lastKey, valueToSet);
        }
    }

    /**
     * Removes a node at the specified JSON path.
     *
     * <p>Navigates through the JSON tree following the dot-separated path
     * and removes the node at the final position. Used for {@code j(absent)} marker.</p>
     *
     * @param processedJson the root JSON node
     * @param path the JSON path in format {@code j(parent.child.field)}
     */
    private void removeNode(JsonNode processedJson, String path) {
        String cleanPath = path.trim();
        String[] jsonParts = cleanPath.split("\\.");
        JsonNode current = processedJson;
        for (int i = 0; i < jsonParts.length - 1; i++) {
            String part = jsonParts[i];
            if (part.endsWith("[]")) {
                // Array parent: items[] -> items
                part = part.substring(0, part.length() - 2);
                current = current.get(part);
            } else if (part.matches(".*\\[\\d+]")) {
                // Indexed: items[0] -> get "items" array, then element at index 0
                String arrayName = part.replaceAll("\\[\\d+]$", "");
                int elementFromArrayIndex = Integer.parseInt(part.replaceAll(".*\\[(\\d+)]$", "$1"));
                current = current.get(arrayName).get(elementFromArrayIndex);
                if (current == null) {
                    return;
                }
            } else {
                // Regular field
                current = current.get(part);
            }
        }
        // Now current points to the parent object, set the last key
        ObjectNode currentObj = (ObjectNode) current;
        String lastKey = jsonParts[jsonParts.length - 1];
        if (lastKey.endsWith("[]")) {
            lastKey = lastKey.substring(0, lastKey.length() - 2);
        } else if (lastKey.matches("^\\w+\\[\\d+]$")) {
            String nodeKey = lastKey.replaceAll("\\[\\d+]", "");
            int currentElementIndex = Integer.parseInt(lastKey.replaceAll("[a-zA-Z\\[\\]]", ""));
            ArrayNode arrayNode = (ArrayNode) currentObj.get(nodeKey);
            arrayNode.remove(currentElementIndex);
            return;
        }
        currentObj.remove(lastKey);
    }

    /**
     * Resolves nested placeholders within a value string.
     *
     * <p>Handles cases where a CSV value contains references to other context variables.
     * Only resolves non-JSON placeholders (those not containing {@code j(}).</p>
     *
     * @param valueForPlaceholder the value that may contain nested placeholders
     * @param scenarioContext the context containing values for resolution
     * @return the value with all nested placeholders resolved
     */
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

    /**
     * Collects all entries from the context map that contain parent markers.
     *
     * <p>Filters the context map to find entries with values starting with
     * {@code j(exists)}, {@code j(null)}, {@code j(empty)}, or {@code j(absent)}.</p>
     *
     * @param result the full context map from scenario context
     * @return a map containing only the entries with parent marker values
     */
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

    /**
     * Checks if a value is a parent marker.
     *
     * @param value the value to check
     * @return {@code true} if the value starts with a recognized marker
     *         ({@code $exists}, {@code $null}, {@code $empty}, or {@code $absent})
     */
    private static boolean isMarker(String value) {
        return value.startsWith(EXISTS_MARKER)
                || value.startsWith(NULL_MARKER)
                || value.startsWith(EMPTY_MARKER)
                || value.startsWith(ABSENT_MARKER);
    }

    private static class ArrayNodeWithIndexHolder {
        private int index;
        private ArrayNode arrayNode;

        public ArrayNodeWithIndexHolder(int index, ArrayNode arrayNode) {
            this.index = index;
            this.arrayNode = arrayNode;
        }
    }

//    private void removeArrayElement(ObjectNode jsonNode, String path) {
//        String cleanPath = path.trim();
//        String nodeKey = cleanPath.replaceAll("\\[\\d+]", "");
//        int currentElementIndex = Integer.parseInt(cleanPath.replaceAll("[a-zA-Z\\[\\]]", ""));
//        ArrayNode arrayNode = (ArrayNode) jsonNode.get(nodeKey);
//        arrayNode.remove(currentElementIndex);
//    }
//
//    private void setArrayElementToEmptyValue(ObjectNode jsonNode, String path) {
//        ArrayNodeWithIndexHolder arrayNodeWithIndexHolder = navigateToArrayElement(jsonNode, path);
//        String cleanPath = path.trim();
//        String nodeKey = cleanPath.replaceAll("\\[\\d+]", "");
//        int currentElementIndex = Integer.parseInt(cleanPath.replaceAll("[a-zA-Z\\[\\]]", ""));
//        ArrayNode arrayNode = (ArrayNode) jsonNode.get(nodeKey);
//        arrayNode.set(currentElementIndex, objectMapper.createObjectNode());
//    }
//
//
//
//    private void setArrayElementToNull(ObjectNode jsonNode, String path) {
//        String cleanPath = path.trim();
//        String nodeKey = cleanPath.replaceAll("\\[\\d+]", "");
//        int currentElementIndex = Integer.parseInt(cleanPath.replaceAll("[a-zA-Z\\[\\]]", ""));
//        ArrayNode arrayNode = (ArrayNode) jsonNode.get(nodeKey);
//        arrayNode.set(currentElementIndex, JsonNodeFactory.instance.nullNode());
//    }
}
