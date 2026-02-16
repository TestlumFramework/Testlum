package com.knubisoft.comparator;

import com.knubisoft.comparator.exception.MatchException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.knubisoft.comparator.ErrorHelper.raise;

public class XMLComparator extends AbstractObjectComparator<Node> {

    public XMLComparator(final Mode mode) {
        super(mode);
    }

    @Override
    public void compare(final Node expected, final Node actual) throws MatchException {
        raise(!Objects.equals(expected.getNodeName(), actual.getNodeName()),
                "Expected node name [" + expected.getNodeName() + "] but was [" + actual.getNodeName() + "]");

        if (expected.hasAttributes()) {
            compareNodes(expected, actual);
        }
        new StringComparator(mode).compare(expected.getNodeValue(), actual.getNodeValue());

        compareChildNodes(expected, actual);
    }

    private void compareChildNodes(final Node expected, final Node actual) throws MatchException {
        if (expected.hasChildNodes()) {
            raise(!actual.hasChildNodes(), "Child nodes not found");
            compareNodesList(expected.getChildNodes(), actual.getChildNodes());
        }
        mode.onStrict(() -> raise(actual.hasChildNodes(), "Additional child nodes found"));
    }


    private void compareNodesList(final NodeList expected, final NodeList actual) throws MatchException {
        raise(expected.getLength() != actual.getLength(), "Child nodes length not equals");

        for (int i = 0, length = expected.getLength(); i < length; i++) {
            compare(expected.item(i), actual.item(i));
        }
    }

    private void compareNodes(final Node expected, final Node actual) throws MatchException {
        raise(!actual.hasAttributes(), "Attributes not found in actual document for node " + actual.getNodeName());
        compareAttributes(expected.getAttributes(), actual.getAttributes());
        mode.onStrict(() -> raise(actual.hasAttributes(),
                "Additional attributes found in actual document for node " + actual.getNodeName()));
    }

    private void compareAttributes(final NamedNodeMap expected, final NamedNodeMap actual) throws MatchException {
        Map<String, String> expectedMap = namedNodeMapToMap(expected);
        Map<String, String> actualMap = namedNodeMapToMap(actual);

        mode.onStrict(() -> validateAttributesLength(expected, actual, expectedMap, actualMap));

        for (Map.Entry<String, String> expectedEntry : expectedMap.entrySet()) {
            String actualValue = actualMap.get(expectedEntry.getKey());
            raise(actualValue == null, "Attribute with name " + expectedMap.keySet());
            new StringComparator(mode).compare(expectedEntry.getValue(), actualValue);
        }
    }

    private void validateAttributesLength(final NamedNodeMap expected,
                                          final NamedNodeMap actual,
                                          final Map<String, String> expectedMap,
                                          final Map<String, String> actualMap) {
        if (expected.getLength() != actual.getLength()) {
            Set<String> props = expectedMap.keySet();
            props.removeAll(actualMap.keySet());
            raise(!props.isEmpty(), "Attributes length not match. Missing property");
            raise("Attributes length not match. Found unexpected property " + String.join(", ", props));
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


