package com.knubisoft.testlum.testing.framework.autohealing.extractor.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericAttributeMetadataExtractor {

    public static Map<String, String> extractAttributes(final String locatorValue) {
        Map<String, String> attributes = new HashMap<>();
        if (locatorValue == null || locatorValue.trim().isEmpty()) {
            return attributes;
        }

        String cleaned = locatorValue.replaceAll("^\\s*\\(", "")
                .replaceAll("\\)\\s*\\[.*?\\]$", "")
                .replaceAll("\\[\\d+\\]$", "");

        int lastSeparator = -1;
        int bracketDepth = 0;
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (c == '[') bracketDepth++;
            else if (c == ']') bracketDepth--;
            else if (bracketDepth == 0) {
                if (c == '/' || c == '>' || c == '+' || c == '~' || Character.isWhitespace(c)) {
                    lastSeparator = i;
                }
            }
        }

        String targetSegment = (lastSeparator != -1) ? cleaned.substring(lastSeparator + 1).trim() : cleaned;

        Pattern attrPattern = Pattern.compile("(?<=[@\\[\\s,])([\\w-]+)\\s*[=,]\\s*['\"]([^'\"]+)['\"]");
        Matcher attrMatcher = attrPattern.matcher(targetSegment);
        while (attrMatcher.find()) {
            attributes.put(attrMatcher.group(1), attrMatcher.group(2));
        }

        if (!cleaned.contains("/")) {
            Pattern shorthandPattern = Pattern.compile("([.#])([\\w-]+)");
            Matcher shorthandMatcher = shorthandPattern.matcher(targetSegment);
            while (shorthandMatcher.find()) {
                String type = shorthandMatcher.group(1).equals(".") ? "class" : "id";
                String value = shorthandMatcher.group(2);
                attributes.merge(type, value, (old, val) -> old.contains(val) ? old : old + " " + val);
            }
        }

        return attributes;
    }

}
