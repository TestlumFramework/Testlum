package com.knubisoft.testlum.testing.framework.autohealing.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class HealingElementMetadata {

    private final List<String> tagNames = new ArrayList<>();
    private final Set<String> texts = new HashSet<>();
    private final Set<String> classes = new HashSet<>();
    private final List<String> ids = new ArrayList<>();
    private final Map<String, String> attributes = new HashMap<>();
    private final List<String> parentTags = new ArrayList<>();
    private final Set<String> ancestorIds = new HashSet<>();

    public void addTagName(final String tagName) {
        this.tagNames.add(tagName);
    }

    public void addText(final String text) {
        this.texts.add(text);
    }

    public void addClass(final String className) {
        this.classes.add(className);
    }

    public void addId(final String id) {
        this.ids.add(id);
    }

    public void addAttributes(final Map<String, String> attributeMap) {
        this.attributes.putAll(attributeMap);
    }

    public void addParentTag(final String parentTag) {
        this.parentTags.add(parentTag);
    }

    public void addAncestorId(final String ancestorId) {
        this.ancestorIds.add(ancestorId);
    }

    public Optional<String> findMostCommonTag() {
        if (tagNames.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Long> counts = tagNames.stream()
                .collect(Collectors.groupingBy(entry -> entry, Collectors.counting()));

        Optional<Map.Entry<String, Long>> maxEntry = counts.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        return maxEntry.map(Map.Entry::getKey);
    }

}
