package com.testlum.testing.framework.autohealing.extractor.locator;

import com.testlum.testing.framework.autohealing.dto.HealingElementMetadata;
import com.testlum.testing.framework.autohealing.extractor.strategy.TagNameExtractor;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LocatorIdMetadataExtractor implements GenericMetadataExtractor, TagNameExtractor {

    private static final Pattern LOCATOR_ID_TAG_PATTERN =
            Pattern.compile("^.*?([A-Z][a-z]+|[A-Z]|[A-Z][0-9])(?=$)");
    private static final Pattern WORD_SPLIT_PATTERN =
            Pattern.compile("(?<!^)(?=[A-Z])|[_-]");

    @Override
    public void extract(final String locatorId, final HealingElementMetadata metadata) {
        extractTagName(locatorId, metadata);
    }

    @Override
    public void extractTagName(final String locatorId, final HealingElementMetadata healingElementMetadata) {
        if (locatorId == null || locatorId.isBlank()) {
            return;
        }
        if (tryExtractDirectTag(locatorId, healingElementMetadata)) {
            return;
        }
        extractFallbackTag(locatorId, healingElementMetadata);
    }

    private boolean tryExtractDirectTag(final String locatorId, final HealingElementMetadata metadata) {
        for (String word : WORD_SPLIT_PATTERN.split(locatorId)) {
            if (HtmlTagRegistry.isDirectHtmlTag(word)) {
                metadata.addTagName(word.toLowerCase());
                return true;
            }
        }
        return false;
    }

    private void extractFallbackTag(final String locatorId, final HealingElementMetadata metadata) {
        Matcher matcher = LOCATOR_ID_TAG_PATTERN.matcher(locatorId);
        if (matcher.find()) {
            String possibleTagName = HtmlTagRegistry.resolveTagName(matcher.group(1));
            if (possibleTagName != null) {
                metadata.addTagName(possibleTagName);
            }
        }
    }
}
