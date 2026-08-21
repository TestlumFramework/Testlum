package com.knubisoft.testlum.testing.framework.xml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

final class XsdMessages {

    private static final String TOO_SHORT = "%s is too short: %s characters, minimum is %s";
    private static final String TOO_LONG = "%s is too long: %s characters, maximum is %s";
    private static final String EXACT_LENGTH = "%s must be exactly %s characters, but has %s";
    private static final String BAD_FORMAT = "%s has an unexpected format";
    private static final String NOT_IN_ENUM = "%s must be one of: %s";
    private static final String TOO_SMALL = "%s must be %s or greater";
    private static final String TOO_LARGE = "%s must be %s or less";
    private static final String WRONG_TYPE = "%s is not a valid %s";

    private static final String BAD_ATTRIBUTE = "Attribute '%s' of <%s> has an invalid value: '%s'";
    private static final String BAD_ELEMENT_VALUE = "Element <%s> has an invalid value: '%s'";
    private static final String UNRESOLVED_TYPE = "Cannot resolve type '%s' for element <%s>";
    private static final String MISSING_ATTRIBUTE = "<%s> is missing required attribute '%s'";
    private static final String UNKNOWN_ATTRIBUTE = "<%s> has no attribute '%s'";
    private static final String NOT_ALLOWED_HERE = "<%s> cannot be used here. Allowed: %s";
    private static final String INCOMPLETE = "<%s> is incomplete. Add one of: %s";
    private static final String NO_CHILD_EXPECTED = "<%s> cannot be used here, no child element is expected";
    private static final String MUST_BE_EMPTY = "<%s> must be empty";
    private static final String NO_TEXT_ALLOWED = "<%s> cannot contain text";
    private static final String UNKNOWN_ELEMENT = "Unknown element <%s>";

    private static final Map<String, String> READABLE_TYPES = Map.of(
            "int", "whole number",
            "integer", "whole number",
            "long", "whole number",
            "short", "whole number",
            "decimal", "number",
            "double", "number",
            "float", "number",
            "boolean", "true/false value");

    private static final Set<String> FACET_CODES = facetRenderers().keySet();

    private static final Map<String, BiFunction<XsdIssue, String, String>> RENDERERS = allRenderers();

    private static final int MAX_LISTED_ELEMENTS = 8;

    private XsdMessages() {
    }

    static boolean isFacet(final String code) {
        return FACET_CODES.contains(code);
    }

    static String render(final XsdIssue issue, final String subject) {
        BiFunction<XsdIssue, String, String> renderer = RENDERERS.get(issue.code());
        return renderer == null ? issue.messageWithoutCode() : renderer.apply(issue, subject);
    }

    private static Map<String, BiFunction<XsdIssue, String, String>> allRenderers() {
        Map<String, BiFunction<XsdIssue, String, String>> all = new HashMap<>(facetRenderers());
        all.putAll(attributeRenderers());
        all.putAll(elementRenderers());
        return Map.copyOf(all);
    }

    private static Map<String, BiFunction<XsdIssue, String, String>> facetRenderers() {
        Map<String, BiFunction<XsdIssue, String, String>> map = new HashMap<>();
        map.put("cvc-minLength-valid", (i, s) -> String.format(TOO_SHORT, s, i.arg(1), i.arg(2)));
        map.put("cvc-maxLength-valid", (i, s) -> String.format(TOO_LONG, s, i.arg(1), i.arg(2)));
        map.put("cvc-length-valid", (i, s) -> String.format(EXACT_LENGTH, s, i.arg(2), i.arg(1)));
        map.put("cvc-pattern-valid", (i, s) -> String.format(BAD_FORMAT, s));
        map.put("cvc-enumeration-valid", (i, s) -> String.format(NOT_IN_ENUM, s, readableList(i.arg(1))));
        map.put("cvc-minInclusive-valid", (i, s) -> String.format(TOO_SMALL, s, i.arg(1)));
        map.put("cvc-maxInclusive-valid", (i, s) -> String.format(TOO_LARGE, s, i.arg(1)));
        map.put("cvc-datatype-valid.1.2.1", (i, s) -> String.format(WRONG_TYPE, s, readableType(i.arg(1))));
        return Map.copyOf(map);
    }

    private static Map<String, BiFunction<XsdIssue, String, String>> attributeRenderers() {
        Map<String, BiFunction<XsdIssue, String, String>> map = new HashMap<>();
        map.put("cvc-attribute.3", (i, s) -> String.format(BAD_ATTRIBUTE, i.arg(1), i.arg(2), i.arg(0)));
        map.put("cvc-type.3.1.3", (i, s) -> String.format(BAD_ELEMENT_VALUE, i.arg(1), i.arg(0)));
        map.put("cvc-elt.4.2", (i, s) -> String.format(UNRESOLVED_TYPE, i.arg(0), i.arg(1)));
        map.put("cvc-complex-type.4", (i, s) -> String.format(MISSING_ATTRIBUTE, i.arg(1), i.arg(0)));
        map.put("cvc-complex-type.3.2.2", (i, s) -> String.format(UNKNOWN_ATTRIBUTE, i.arg(1), i.arg(0)));
        return Map.copyOf(map);
    }

    private static Map<String, BiFunction<XsdIssue, String, String>> elementRenderers() {
        Map<String, BiFunction<XsdIssue, String, String>> map = new HashMap<>();
        map.put("cvc-complex-type.2.4.a", (i, s) -> String.format(NOT_ALLOWED_HERE, name(i.arg(0)), listOf(i)));
        map.put("cvc-complex-type.2.4.b", (i, s) -> String.format(INCOMPLETE, name(i.arg(0)), listOf(i)));
        map.put("cvc-complex-type.2.4.c", (i, s) -> String.format(UNKNOWN_ELEMENT, name(i.arg(0))));
        map.put("cvc-complex-type.2.4.d", (i, s) -> String.format(NO_CHILD_EXPECTED, name(i.arg(0))));
        map.put("cvc-complex-type.2.1", (i, s) -> String.format(MUST_BE_EMPTY, name(i.arg(0))));
        map.put("cvc-complex-type.2.3", (i, s) -> String.format(NO_TEXT_ALLOWED, name(i.arg(0))));
        map.put("cvc-elt.1", (i, s) -> String.format(UNKNOWN_ELEMENT, name(i.arg(0))));
        map.put("cvc-elt.1.a", (i, s) -> String.format(UNKNOWN_ELEMENT, name(i.arg(0))));
        return Map.copyOf(map);
    }

    private static String listOf(final XsdIssue issue) {
        return readableList(issue.arg(1));
    }

    private static String readableType(final String type) {
        return type == null ? "value" : READABLE_TYPES.getOrDefault(name(type), name(type));
    }

    private static String readableList(final String raw) {
        if (raw == null) {
            return "";
        }
        String[] names = Arrays.stream(raw.split(","))
                .map(XsdMessages::name)
                .filter(entry -> !entry.isEmpty())
                .toArray(String[]::new);
        String listed = Arrays.stream(names).limit(MAX_LISTED_ELEMENTS).collect(Collectors.joining(", "));
        return names.length > MAX_LISTED_ELEMENTS
                ? listed + String.format(" ... and %d more", names.length - MAX_LISTED_ELEMENTS)
                : listed;
    }

    private static String name(final String qualified) {
        if (qualified == null) {
            return "";
        }
        String trimmed = qualified.replaceAll("[{}\\[\\]\"]", "").trim();
        int lastColon = trimmed.lastIndexOf(':');
        return lastColon < 0 ? trimmed : trimmed.substring(lastColon + 1).trim();
    }
}
