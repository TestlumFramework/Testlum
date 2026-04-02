package com.knubisoft.comparator;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeType;
import com.knubisoft.comparator.exception.MatchException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonComparator extends AbstractObjectComparator<JsonNode> {

    private final StringComparator stringComparator;

    public JsonComparator(final Mode mode) {
        super(mode);
        this.stringComparator = new StringComparator(mode);
    }

    @Override
    public void compare(final JsonNode expected, final JsonNode actual) throws MatchException {
        JsonNodeType expectedType = expected.getNodeType();
        JsonNodeType actualType = actual.getNodeType();
        compareByType(expected, actual, expectedType, actualType);
    }

    private void compareByType(final JsonNode expected, final JsonNode actual,
                               final JsonNodeType expType, final JsonNodeType actType) throws MatchException {
        if (expType == actType) {
            compareSameType(expected, actual, expType);
        } else if (isStringToComparableType(expType, actType)) {
            stringComparator.compare(expected.asString(), actual.asString());
        } else {
            raiseTypeMismatch(expType, actType);
        }
    }

    private void compareSameType(final JsonNode expected, final JsonNode actual,
                                 final JsonNodeType type) throws MatchException {
        switch (type) {
            case NULL, MISSING -> { }
            case BOOLEAN -> compareBoolean(expected, actual);
            case NUMBER -> compareNumber(expected, actual);
            case STRING -> stringComparator.compare(expected.asString(), actual.asString());
            case ARRAY -> compareArray(expected, actual);
            case OBJECT -> compareObject(expected, actual);
            default -> raiseTypeMismatch(type, type);
        }
    }

    private void raiseTypeMismatch(final JsonNodeType expectedType, final JsonNodeType actualType) {
        ErrorHelper.raise(String.format("Expected [%s] but was [%s]", expectedType, actualType));
    }

    private boolean isStringToComparableType(final JsonNodeType expectedType, final JsonNodeType actualType) {
        return expectedType == JsonNodeType.STRING
                && (actualType == JsonNodeType.STRING
                    || actualType == JsonNodeType.NUMBER
                    || actualType == JsonNodeType.BOOLEAN);
    }

    private void compareArray(final JsonNode expected, final JsonNode actual) throws MatchException {
        compareElements(iteratorToList(expected.iterator()), iteratorToList(actual.iterator()));
    }

    private void compareObject(final JsonNode expected, final JsonNode actual) throws MatchException {
        compareFields(
                iteratorToList(expected.properties().iterator()),
                iteratorToList(actual.properties().iterator()));
    }

    private void compareBoolean(final JsonNode expected, final JsonNode actual) {
        ErrorHelper.raise(expected.asBoolean() != actual.asBoolean(),
                String.format("Property [%s] is not equal to [%s]", expected.asString(), actual.asString()));
    }

    private void compareNumber(final JsonNode expected, final JsonNode actual) {
        ErrorHelper.raise(!new BigDecimal(expected.asString()).equals(new BigDecimal(actual.asString())),
                String.format("Property [%s] is not equal to [%s]", expected.asString(), actual.asString()));
    }

    private <T> List<T> iteratorToList(final Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }

    private void compareElements(final List<JsonNode> expected, final List<JsonNode> actual) throws MatchException {
        ErrorHelper.raise(expected.size() != actual.size(),
                String.format("Expected array length is [%d] actual [%d]", expected.size(), actual.size()));

        for (int i = 0, size = expected.size(); i < size; i++) {
            JsonNode expectedItem = expected.get(i);
            JsonNode actualItem = actual.get(i);

            compare(expectedItem, actualItem);
        }
    }

    private void compareFields(final List<Map.Entry<String, JsonNode>> expected,
                               final List<Map.Entry<String, JsonNode>> actual) throws MatchException {
        mode.onStrict(() -> ErrorHelper.raise(expected.size() != actual.size(),
                "Difference in properties or count Missing"));

        Map<String, JsonNode> actualMap = toMap(actual);
        for (Map.Entry<String, JsonNode> expectedEntry : expected) {
            JsonNode actualValue = actualMap.get(expectedEntry.getKey());
            if (actualValue == null) {
                ErrorHelper.raise(String.format("Property with name [%s] not found", expectedEntry.getKey()));
            }
            compare(expectedEntry.getValue(), actualValue);
        }
    }

    private Map<String, JsonNode> toMap(final List<Map.Entry<String, JsonNode>> entries) {
        Map<String, JsonNode> map = new LinkedHashMap<>();
        entries.forEach(e -> map.put(e.getKey(), e.getValue()));
        return map;
    }
}
