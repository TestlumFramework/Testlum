package com.knubisoft.testlum.testing.framework.autohealing.extractor.locator;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealingElementMetadata;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.strategy.TagNameExtractor;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LocatorIdMetadataExtractor implements GenericMetadataExtractor, TagNameExtractor {

    private static final Pattern LOCATOR_ID_TAG_PATTERN =
            Pattern.compile("^.*?([A-Z][a-z]+|[A-Z]|[A-Z][0-9])(?=$)");

    @Override
    public void extract(final String locatorId, final HealingElementMetadata metadata) {
        extractTagName(locatorId, metadata);
    }

    @Override
    public void extractTagName(final String locatorId, final HealingElementMetadata healingElementMetadata) {
        Matcher matcher = LOCATOR_ID_TAG_PATTERN.matcher(locatorId);
        if (matcher.find()) {
            String possibleTagName = HtmlTagRegistry.resolveTagName(matcher.group(1));
            if (possibleTagName != null) {
                healingElementMetadata.addTagName(possibleTagName);
            }
        }
    }
}
