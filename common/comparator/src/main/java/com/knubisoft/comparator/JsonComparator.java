package com.knubisoft.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.knubisoft.comparator.exception.MatchException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.knubisoft.comparator.ErrorHelper.raise;

public class JsonComparator extends AbstractObjectComparator<JsonNode> {

    public JsonComparator(final Mode mode) {
        super(mode);
    }

    //CHECKSTYLE:OFF
    @Override
    public void compare(final JsonNode expected, final JsonNode actual) throws MatchException {
        JsonNodeType expectedType = expected.getNodeType();
        JsonNodeType actualType = actual.getNodeType();

        if (expectedType == JsonNodeType.BOOLEAN && actualType == JsonNodeType.BOOLEAN) {
            raise(expected.asBoolean() != actual.asBoolean(),
                    "Property [" + expected.asText() + "] is not equal to [" + actual.asText() + "]");

        } else if (expectedType == JsonNodeType.NUMBER && actualType == JsonNodeType.NUMBER) {

            raise(!new BigDecimal(expected.asText()).equals(new BigDecimal(actual.asText())),
                    "Property [" + expected.asText() + "] is not equal to [" + actual.asText() + "]");

        } else if (expectedType == JsonNodeType.STRING && actualType == JsonNodeType.STRING
                || expectedType == JsonNodeType.STRING && actualType == JsonNodeType.NUMBER
                || expectedType == JsonNodeType.STRING && actualType == JsonNodeType.BOOLEAN) {
            new StringComparator(mode).compare(expected.asText(), actual.asText());

        } else if (expectedType == JsonNodeType.ARRAY && actualType == JsonNodeType.ARRAY) {
            compareElements(
                    iteratorToList(expected.elements()),
                    iteratorToList(actual.elements()));

        } else if (expectedType == JsonNodeType.OBJECT && actualType == JsonNodeType.OBJECT) {
            compareFields(
                    iteratorToList(expected.properties().iterator()),
                    iteratorToList(actual.properties().iterator()));

        } else {
            raise(expected.getNodeType() != actual.getNodeType(),
                    "Expected [" + expected.getNodeType() + "] but was [" + actual.getNodeType() + "]");
        }
    }
    //CHECKSTYLE:ON

    private <T> List<T> iteratorToList(final Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }

    private void compareElements(final List<JsonNode> expected, final List<JsonNode> actual) throws MatchException {
        raise(expected.size() != actual.size(),
                "Expected array length is [" + expected.size() + "] actual [" + actual.size() + "]");

        for (int i = 0, size = expected.size(); i < size; i++) {
            JsonNode expectedItem = expected.get(i);
            JsonNode actualItem = actual.get(i);

            compare(expectedItem, actualItem);
        }
    }

    private void compareFields(final List<Map.Entry<String, JsonNode>> expected,
                               final List<Map.Entry<String, JsonNode>> actual) throws MatchException {
        mode.onStrict(() -> raise(expected.size() != actual.size(),
                "Difference in properties or count Missing"));

        for (Map.Entry<String, JsonNode> expectedEntry : expected) {
            JsonNode actualValue = getActualValue(actual, expectedEntry);
            validateActualValue(expectedEntry, actualValue);
            compare(expectedEntry.getValue(), actualValue);
        }
    }

    private void validateActualValue(final Map.Entry<String, JsonNode> expectedEntry, final JsonNode actualValue) {
        if (actualValue == null) {
            raise("Property with name [" + expectedEntry.getKey() + "] not found");
        }
    }

    private JsonNode getActualValue(final List<Map.Entry<String, JsonNode>> actual,
                                    final Map.Entry<String, JsonNode> expectedEntry) {
        JsonNode actualValue = null;
        for (Map.Entry<String, JsonNode> actualEntry : actual) {
            if (expectedEntry.getKey().equals(actualEntry.getKey())) {
                actualValue = actualEntry.getValue();
                break;
            }
        }
        return actualValue;
    }
}
