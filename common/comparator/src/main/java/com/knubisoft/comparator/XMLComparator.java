package com.knubisoft.comparator;

import com.knubisoft.comparator.exception.MatchException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class XMLComparator extends AbstractObjectComparator<Node> {

    private final StringComparator stringComparator;

    public XMLComparator(final Mode mode) {
        super(mode);
        this.stringComparator = new StringComparator(mode);
    }

    @Override
    public void compare(final Node expected, final Node actual) throws MatchException {
        ErrorHelper.raise(!Objects.equals(expected.getNodeName(), actual.getNodeName()),
                String.format("Expected node name [%s] but was [%s]", expected.getNodeName(), actual.getNodeName()));

        if (expected.hasAttributes()) {
            compareNodes(expected, actual);
        }
        stringComparator.compare(expected.getNodeValue(), actual.getNodeValue());

        compareChildNodes(expected, actual);
    }

    private void compareChildNodes(final Node expected, final Node actual) throws MatchException {
        if (expected.hasChildNodes()) {
            ErrorHelper.raise(!actual.hasChildNodes(), "Child nodes not found");
            compareNodesList(expected.getChildNodes(), actual.getChildNodes());
        }
        mode.onStrict(() -> ErrorHelper.raise(actual.hasChildNodes(), "Additional child nodes found"));
    }


    private void compareNodesList(final NodeList expected, final NodeList actual) throws MatchException {
        ErrorHelper.raise(expected.getLength() != actual.getLength(), "Child nodes length not equals");

        for (int i = 0, length = expected.getLength(); i < length; i++) {
            compare(expected.item(i), actual.item(i));
        }
    }

    private void compareNodes(final Node expected, final Node actual) throws MatchException {
        ErrorHelper.raise(!actual.hasAttributes(),
                String.format("Attributes not found in actual document for node %s", actual.getNodeName()));
        compareAttributes(expected.getAttributes(), actual.getAttributes());
        mode.onStrict(() -> ErrorHelper.raise(actual.hasAttributes(),
                String.format("Additional attributes found in actual document for node %s", actual.getNodeName())));
    }

    private void compareAttributes(final NamedNodeMap expected, final NamedNodeMap actual) throws MatchException {
        Map<String, String> expectedMap = namedNodeMapToMap(expected);
        Map<String, String> actualMap = namedNodeMapToMap(actual);

        mode.onStrict(() -> validateAttributesLength(expected, actual, expectedMap, actualMap));

        for (Map.Entry<String, String> expectedEntry : expectedMap.entrySet()) {
            String actualValue = actualMap.get(expectedEntry.getKey());
            ErrorHelper.raise(actualValue == null,
                    String.format("Attribute with name [%s] not found", expectedEntry.getKey()));
            stringComparator.compare(expectedEntry.getValue(), actualValue);
        }
    }

    private void validateAttributesLength(final NamedNodeMap expected,
                                          final NamedNodeMap actual,
                                          final Map<String, String> expectedMap,
                                          final Map<String, String> actualMap) {
        if (expected.getLength() != actual.getLength()) {
            Set<String> missing = new HashSet<>(expectedMap.keySet());
            missing.removeAll(actualMap.keySet());
            ErrorHelper.raise(!missing.isEmpty(),
                    String.format("Attributes length not match. Missing property: %s", String.join(", ", missing)));
            Set<String> unexpected = new HashSet<>(actualMap.keySet());
            unexpected.removeAll(expectedMap.keySet());
            ErrorHelper.raise(!unexpected.isEmpty(),
                    String.format("Attributes length not match. Found unexpected property: %s",
                            String.join(", ", unexpected)));
        }
    }

    private Map<String, String> namedNodeMapToMap(final NamedNodeMap nodeMap) {
        Map<String, String> map = new HashMap<>();

        for (int i = 0, length = nodeMap.getLength(); i < length; i++) {
            Node node = nodeMap.item(i);
            map.put(node.getNodeName(), node.getNodeValue());
        }
        return map;
    }
}


