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
 * placeholder syntax {@code {{path}}} where values from CSV variations are injected.</p>
 *
 * <h2>Placeholder Syntax</h2>
 * <ul>
 *   <li>{@code "field": "{{fieldName}}"}      — quoted placeholder for string values</li>
 *   <li>{@code "field": {{fieldName}}}         — unquoted placeholder for non-string values (numbers, booleans, arrays)</li>
 *   <li>{@code "items": [{{items[]}}]}         — array placeholder for injecting array elements</li>
 * </ul>
 *
 * <h2>Parent Markers</h2>
 * <p>Special markers control parent node behavior in the JSON structure:</p>
 * <ul>
 *   <li>{@code $exists} — parent node exists normally, children are injected</li>
 *   <li>{@code $null}   — parent node is set to JSON null</li>
 *   <li>{@code $empty}  — parent node is set to empty object {@code {}} or empty array {@code []}</li>
 *   <li>{@code $absent} — parent node is completely removed from the JSON</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>
 * JSON template:
 * {"user": {"name": "{{user.name}}", "age": {{user.age}}}, "tags": [{{tags[]}}]}
 *
 * CSV variations:
 * user    | user.name | user.age | tags[]
 * $exists | John      | 25       | "a","b","c"
 * $null   | -         | -        | -
 * $absent | -         | -        | -
 * </pre>
 *
 * @see ScenarioContextVariationInjectionStrategy
 */
public class JsonVariationInjectionStrategy implements ScenarioContextVariationInjectionStrategy {

    /** Matches the {@code "raw": "..."} field in the scenario step JSON envelope. */
    private static final String RAW_NODE_IN_BODY_REGEXP = "(\"raw\"\\s*:\\s*\")(?:[^\"\\\\]|\\\\.)*(\")";
    private static final Pattern RAW_PATTERN = Pattern.compile(RAW_NODE_IN_BODY_REGEXP);

    /** Matches any {@code {{...}}} placeholder. */
    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);

    private static final String ABSENT_MARKER = "$absent";
    private static final String NULL_MARKER = "$null";
    private static final String EMPTY_MARKER = "$empty";
    private static final String EXISTS_MARKER = "$exists";

    private static final PlaceholderNormalizer placeholderNormalizer = new PlaceholderNormalizer();

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    /**
     * Cached between {@link #isApplicable} and {@link #injectVariationsValues}.
     * Set during the applicability check to avoid re-parsing the scenario step.
     */
    private JsonNode currentRawJson;

    /**
     * Returns {@code true} if this strategy can handle the given scenario step.
     *
     * <p>Applicable when the step contains a {@code post}, {@code put}, or {@code patch}
     * HTTP method whose {@code body.raw} field holds at least one {@code {{...}}} placeholder.</p>
     *
     * <p><strong>Side effect:</strong> on a positive match, caches the raw JSON node in
     * {@link #currentRawJson} so that {@link #injectVariationsValues} can reuse it without
     * re-parsing the scenario step.</p>
     *
     * @param scenarioStepAsString the scenario step serialized as a JSON string
     * @return {@code true} if the step has a supported HTTP method with JSON placeholders
     */
    @Override
    public boolean isApplicable(final String scenarioStepAsString) {
        boolean isApplicableStrategy = false;
        try {
            JsonNode jsonNode = objectMapper.readValue(scenarioStepAsString, JsonNode.class);
            for (String method : List.of("post", "put", "patch")) {
                if (jsonNode.get(method) != null) {
                    JsonNode jsonBodyNode = jsonNode.get(method).get("body");
                    if (jsonBodyNode == null) {
                        continue;
                    }
                    isApplicableStrategy = checkIfPlaceholdersExistsInJsonBody(jsonBodyNode.get("raw"));
                    return isApplicableStrategy;
                }
            }
        } catch (Exception e) {
            return isApplicableStrategy;
        }
        return isApplicableStrategy;
    }

    /**
     * Checks whether the raw JSON body node contains any {@code {{...}}} placeholders.
     *
     * <p>If placeholders are found, caches {@code rawJsonNode} in {@link #currentRawJson}
     * for later use during injection.</p>
     *
     * @param rawJsonNode the {@code raw} body node from the scenario step
     * @return {@code true} if at least one placeholder is present
     */
    private boolean checkIfPlaceholdersExistsInJsonBody(final JsonNode rawJsonNode) {
        Matcher m = ROUTE_PATTERN.matcher(rawJsonNode.asText());
        if (m.find()) {
            currentRawJson = rawJsonNode;
            return true;
        }
        return false;
    }

    /**
     * Injects CSV variation values into the JSON body placeholders of the given scenario step.
     *
     * <p>Injection steps:</p>
     * <ol>
     *   <li>Collect parent markers ({@code $null}, {@code $empty}, {@code $absent}, {@code $exists})
     *       from the scenario context map.</li>
     *   <li>Normalize unquoted placeholders — wraps {@code {{...}}} that are not surrounded by
     *       quotes with {@code "__RAW__{{...}}__RAW__"} so Jackson can parse the body as valid JSON.</li>
     *   <li>Apply parent marker mutations to the parsed JSON tree
     *       (see {@link #applyMarkers} / {@link #applyMarkersToRootArray}).</li>
     *   <li>Replace remaining {@code {{...}}} placeholders with actual CSV values.
     *       Child placeholders whose parent was mutated to {@code $null}/{@code $empty}/{@code $absent}
     *       are skipped — only {@code $exists} or marker-free parents proceed to injection.</li>
     *   <li>Escape the final JSON string and splice it back into the {@code "raw": "..."} field
     *       of the original scenario step.</li>
     * </ol>
     *
     * @param scenarioStepAsString the original scenario step JSON string
     * @param scenarioContext      the context holding CSV variation values for this test row
     * @param escapeSpelQuotes     unused in this strategy; present to satisfy the interface contract
     * @return the scenario step with all placeholders replaced by their variation values
     */
    @SneakyThrows
    @Override
    public String injectVariationsValues(final String scenarioStepAsString,
                                         final ScenarioContext scenarioContext,
                                         final boolean escapeSpelQuotes) {
        Map<String, String> parentMarkers = collectMarkedJsonPairs(scenarioContext.getContextMap());

        String jsonWithRawPlaceholdersIfAny =
                placeholderNormalizer.replacePlaceholdersWithoutQuotesWithRawMark(currentRawJson.asText());
        JsonNode root = objectMapper.readTree(jsonWithRawPlaceholdersIfAny);

        JsonNode preProcessedJsonNode;
        if (root.isObject()) {
            preProcessedJsonNode = applyMarkers((ObjectNode) root, parentMarkers);
        } else if (root instanceof ArrayNode) {
            preProcessedJsonNode = applyMarkersToRootArray(root, parentMarkers);
        } else {
            preProcessedJsonNode = root;
        }

        String ejectedJson = objectMapper.writeValueAsString(preProcessedJsonNode);
        Matcher m = ROUTE_PATTERN.matcher(ejectedJson);
        while (m.find()) {
            String scenarioPlaceholder = m.group(0);
            String csvColumnName = m.group(1);
            String valueForPlaceholder = scenarioContext.get(csvColumnName);

            int lastPathDelimiter = csvColumnName.lastIndexOf(".");
            String parent = lastPathDelimiter > 0 ? csvColumnName.substring(0, lastPathDelimiter) : null;
            String parentMarker = parentMarkers.get(parent);

            // Skip if parent was already mutated to null/empty/absent — only inject for $exists or no marker
            if (parentMarker == null || parentMarker.contains(EXISTS_MARKER)) {
                valueForPlaceholder = resolveNestedPlaceholders(valueForPlaceholder, scenarioContext);
                ejectedJson = placeholderNormalizer.denormalize(scenarioPlaceholder, ejectedJson, valueForPlaceholder);
            }
        }

        String bodyReplacementJson = StringEscapeUtils.escapeJson(ejectedJson);
        Matcher matcher = RAW_PATTERN.matcher(scenarioStepAsString);
        return matcher.replaceFirst("$1" + Matcher.quoteReplacement(bodyReplacementJson) + "$2");
    }

    /**
     * Applies parent marker mutations to the JSON tree.
     * {@code $exists} falls through intentionally — its children are injected in the placeholder loop.
     *
     * @param jsonNode      the root object node of the JSON body
     * @param parentMarkers map of CSV column paths to their marker values
     * @return the mutated JSON node
     */
    private JsonNode applyMarkers(final ObjectNode jsonNode, final Map<String, String> parentMarkers) {
        for (Map.Entry<String, String> entry : parentMarkers.entrySet()) {
            String path = entry.getKey();
            String marker = entry.getValue();
            if (marker.contains(NULL_MARKER)) {
                if (isIndexedArrayPath(path)) {
                    applyToArrayElement(jsonNode, path, (arr, idx) -> arr.set(idx, JsonNodeFactory.instance.nullNode()));
                } else if (isArrayPath(path)) {
                    setAtPath(jsonNode, path, null, true);
                } else {
                    setAtPath(jsonNode, path, null, false);
                }
            } else if (marker.contains(EMPTY_MARKER)) {
                if (isArrayPath(path)) {
                    setAtPath(jsonNode, path, objectMapper.createArrayNode(), true);
                } else if (isIndexedArrayPath(path)) {
                    applyToArrayElement(jsonNode, path, (arr, idx) -> {
                        JsonNode current = arr.get(idx);
                        arr.set(idx, current.isArray() ? objectMapper.createArrayNode() : objectMapper.createObjectNode());
                    });
                } else {
                    setAtPath(jsonNode, path, objectMapper.createObjectNode(), false);
                }
            } else if (marker.contains(ABSENT_MARKER)) {
                if (isIndexedArrayPath(path)) {
                    applyToArrayElement(jsonNode, path, ArrayNode::remove);
                } else {
                    removeAtPath(jsonNode, path);
                }
            }
        }
        return jsonNode;
    }

    /**
     * Applies parent marker mutations when the JSON body root is an {@link ArrayNode}.
     *
     * <p>Only {@code $null} and {@code $empty} are meaningful at the root level:
     * {@code $null} replaces the entire root with a JSON null node;
     * {@code $empty} replaces it with an empty array.
     * {@code $absent} has no effect at the root level.</p>
     *
     * @param root          the root array node of the JSON body
     * @param parentMarkers map of CSV column paths to their marker values
     * @return the mutated root node, or the original node if no applicable marker is found
     */
    private JsonNode applyMarkersToRootArray(final JsonNode root, final Map<String, String> parentMarkers) {
        for (String marker : parentMarkers.values()) {
            if (marker.contains(NULL_MARKER)) {
                return JsonNodeFactory.instance.nullNode();
            } else if (marker.contains(EMPTY_MARKER)) {
                return objectMapper.createArrayNode();
            }
        }
        return root;
    }

    /**
     * Navigates to the parent node for a dot-separated path.
     * Handles three segment types:
     * <ul>
     *   <li>{@code field}    — plain object field, descend into child</li>
     *   <li>{@code field[]}  — array field; strip {@code []} and descend into the array node</li>
     *   <li>{@code field[n]} — indexed; get array by name, then element at index n</li>
     * </ul>
     * Returns {@code null} if any intermediate node is missing.
     *
     * @param root      the root JSON node to start traversal from
     * @param pathParts the dot-split segments of the full path
     * @return the parent node of the last path segment, or {@code null} if unreachable
     */
    private JsonNode navigateToParent(final JsonNode root, final String[] pathParts) {
        JsonNode current = root;
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            if (part.endsWith("[]")) {
                current = current.get(part.substring(0, part.length() - 2));
            } else if (part.matches(".*\\[\\d+]")) {
                String arrayName = part.replaceAll("\\[\\d+]$", "");
                int index = Integer.parseInt(part.replaceAll(".*\\[(\\d+)]$", "$1"));
                current = current.get(arrayName);
                if (current == null) {
                    return null;
                }
                current = current.get(index);
            } else {
                current = current.get(part);
            }
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /**
     * Sets or nulls the node at the given dot-separated path.
     *
     * <p>When {@code valueToSet} is {@code null}, the target field is set to a JSON null node
     * rather than being removed.</p>
     *
     * @param root          the root JSON node to mutate
     * @param path          dot-separated field path (e.g. {@code "user.address.street"},
     *                      {@code "items[]"}, {@code "items[0]"})
     * @param valueToSet    the node to place at the target position, or {@code null} to set JSON null
     * @param isArrayTarget {@code true} when the last segment targets an array field
     *                      ({@code field[]} sets the whole array; {@code field[n]} sets one element)
     */
    private void setAtPath(final JsonNode root,
                           final String path,
                           final JsonNode valueToSet,
                           final boolean isArrayTarget) {
        String[] parts = path.trim().split("\\.");
        JsonNode parent = navigateToParent(root, parts);
        if (parent == null) {
            return;
        }
        ObjectNode parentObj = (ObjectNode) parent;
        String lastKey = parts[parts.length - 1].trim();

        if (isArrayTarget) {
            if (lastKey.endsWith("[]")) {
                lastKey = lastKey.substring(0, lastKey.length() - 2);
            } else if (lastKey.matches("^\\w+\\[\\d+]$")) {
                String nodeKey = lastKey.replaceAll("\\[\\d+]", "");
                int idx = Integer.parseInt(lastKey.replaceAll("[a-zA-Z\\[\\]]", ""));
                ArrayNode arrayNode = (ArrayNode) parent.get(nodeKey);
                if (valueToSet == null) {
                    arrayNode.setNull(idx);
                } else {
                    arrayNode.remove(idx);
                    arrayNode.set(idx, valueToSet);
                }
                parentObj.set(nodeKey, arrayNode);
                return;
            }
        }

        if (valueToSet == null) {
            parentObj.putNull(lastKey);
        } else {
            parentObj.set(lastKey, valueToSet);
        }
    }

    /**
     * Removes the node at the given dot-separated path from its parent.
     *
     * <p>Handles plain fields, array fields ({@code field[]}), and indexed array elements
     * ({@code field[n]}). Does nothing if any intermediate node along the path is missing.</p>
     *
     * @param root the root JSON node to mutate
     * @param path dot-separated field path to the node to remove
     */
    private void removeAtPath(final JsonNode root, final String path) {
        String[] parts = path.trim().split("\\.");
        JsonNode parent = navigateToParent(root, parts);
        if (parent == null) {
            return;
        }
        ObjectNode parentObj = (ObjectNode) parent;
        String lastKey = parts[parts.length - 1];

        if (lastKey.endsWith("[]")) {
            parentObj.remove(lastKey.substring(0, lastKey.length() - 2));
        } else if (lastKey.matches("^\\w+\\[\\d+]$")) {
            String nodeKey = lastKey.replaceAll("\\[\\d+]", "");
            int idx = Integer.parseInt(lastKey.replaceAll("[a-zA-Z\\[\\]]", ""));
            ((ArrayNode) parentObj.get(nodeKey)).remove(idx);
        } else {
            parentObj.remove(lastKey);
        }
    }

    /**
     * Navigates to a specific array element and applies the given action to it.
     *
     * <p>The path must end with an indexed segment, e.g. {@code "items[0]"} or
     * {@code "user.tags[2]"}. All intermediate segments are traversed via
     * {@link #navigateToParent}. Does nothing if the path cannot be resolved or
     * the target is not an {@link ArrayNode}.</p>
     *
     * @param root   the root JSON node to traverse
     * @param path   dot-separated path ending with {@code [n]}
     * @param action the operation to apply to the resolved array and element index
     */
    private void applyToArrayElement(final JsonNode root, final String path, final ArrayElementAction action) {
        String[] parts = path.trim().split("\\.");
        JsonNode parent = navigateToParent(root, parts);
        if (parent == null) {
            return;
        }
        String lastPart = parts[parts.length - 1];
        String arrayName = lastPart.replaceAll("\\[\\d+]$", "");
        int index = Integer.parseInt(lastPart.replaceAll(".*\\[(\\d+)]$", "$1"));
        JsonNode arrayNode = parent.get(arrayName);
        if (arrayNode instanceof ArrayNode arr) {
            action.apply(arr, index);
        }
    }

    /**
     * Operation to apply to a specific element within an {@link ArrayNode}.
     *
     * @see #applyToArrayElement(JsonNode, String, ArrayElementAction)
     */
    @FunctionalInterface
    private interface ArrayElementAction {
        void apply(ArrayNode array, int index);
    }

    /** Path ends with {@code []} — targets an array field as a whole. */
    private boolean isArrayPath(final String path) {
        return path.endsWith("[]");
    }

    /** Path ends with {@code [n]} — targets a specific element within an array. */
    private boolean isIndexedArrayPath(final String path) {
        return path.matches(".*\\[\\d+]$");
    }

    /**
     * Resolves any {@code {{placeholder}}} references that appear inside a CSV cell value.
     *
     * <p>Any {@code {{...}}} found within the value is treated as a context variable reference
     * and substituted from the scenario context.</p>
     *
     * @param valueForPlaceholder the raw CSV value, which may itself contain {@code {{...}}} references
     * @param scenarioContext      the context from which nested placeholder values are resolved
     * @return the value with all nested placeholders substituted
     */
    private String resolveNestedPlaceholders(final String valueForPlaceholder,
                                             final ScenarioContext scenarioContext) {
        Matcher nestedMatcher = ROUTE_PATTERN.matcher(valueForPlaceholder);
        String replaced = valueForPlaceholder;
        while (nestedMatcher.find()) {
            String nestedKey = nestedMatcher.group(1);
            replaced = replaced.replace(nestedMatcher.group(0), scenarioContext.get(nestedKey));
        }
        return replaced;
    }

    /**
     * Filters the scenario context map to return only entries whose value is a parent marker.
     *
     * <p>Preserves insertion order ({@link LinkedHashMap}) so markers are applied
     * in the same sequence as their CSV columns.</p>
     *
     * @param contextMap the full context map from the scenario (CSV column → value)
     * @return a map containing only marker entries ({@code $exists}, {@code $null},
     *         {@code $empty}, {@code $absent})
     */
    private static Map<String, String> collectMarkedJsonPairs(final Map<String, String> contextMap) {
        Map<String, String> parentMarkers = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : contextMap.entrySet()) {
            if (isMarker(entry.getValue())) {
                parentMarkers.put(entry.getKey(), entry.getValue());
            }
        }
        return parentMarkers;
    }

    /**
     * Returns {@code true} if the given value is one of the recognized parent markers.
     *
     * @param value the context value to test
     * @return {@code true} for {@code $exists}, {@code $null}, {@code $empty}, or {@code $absent}
     */
    private static boolean isMarker(final String value) {
        return value.startsWith(EXISTS_MARKER)
                || value.startsWith(NULL_MARKER)
                || value.startsWith(EMPTY_MARKER)
                || value.startsWith(ABSENT_MARKER);
    }
}