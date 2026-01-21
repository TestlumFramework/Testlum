package com.knubisoft.testlum.testing.framework.autohealing.extractor.cssSelector;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealingElementMetadata;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.strategy.AttributeExtractor;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.strategy.ClassExtractor;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.strategy.IdExtractor;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.SpecificMetadataExtractor;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.strategy.TagNameExtractor;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.util.GenericAttributeMetadataExtractor;
import com.knubisoft.testlum.testing.model.pages.CssSelector;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CssSelectorMetadataExtractor implements SpecificMetadataExtractor<CssSelector>, TagNameExtractor, AttributeExtractor, IdExtractor, ClassExtractor {

    private static final Pattern TYPE_SELECTOR_PATTERN =
            Pattern.compile("^([a-zA-Z][a-zA-Z0-9_-]*\\|)?([a-zA-Z][a-zA-Z0-9_-]*)");

    @Override
    public void extractMetadata(final CssSelector type, final HealingElementMetadata metadata) {
        this.extractAttributes(type.getValue(), metadata);
        this.extractClass(type.getValue(), metadata);
        this.extractId(type.getValue(), metadata);
        this.extractTagName(type.getValue(), metadata);
    }

    @Override
    public void extractAttributes(final String cssSelector, final HealingElementMetadata healingElementMetadata) {
        Map<String, String> attributes = GenericAttributeMetadataExtractor.extractAttributes(cssSelector);
        healingElementMetadata.addAttributes(attributes);
    }

    @Override
    public void extractClass(final String cssSelector, final HealingElementMetadata healingElementMetadata) {
        String[] parts = cssSelector.split("[\\s>+~]+");
        String lastPart = parts[parts.length - 1];

        Pattern pattern = Pattern.compile("\\.([_a-zA-Z0-9-]+)");
        Matcher matcher = pattern.matcher(lastPart);

        while (matcher.find()) {
            healingElementMetadata.addClass(matcher.group(1));
        }
    }

    @Override
    public void extractId(final String cssSelector, final HealingElementMetadata healingElementMetadata) {
        String[] parts = cssSelector.split("[\\s>+~]+");
        String lastPart = parts[parts.length - 1];

        Pattern pattern = Pattern.compile("#([_a-zA-Z0-9-]+)");
        Matcher matcher = pattern.matcher(lastPart);

        if (matcher.find()) {
            healingElementMetadata.addId(matcher.group(1));
        }
    }

    @Override
    public void extractTagName(final String cssSelector, final HealingElementMetadata healingElementMetadata) {
        String[] selectorGroups = cssSelector.split(",");

        for (String group : selectorGroups) {
            String[] compounds = group.trim().split("\\s+|>|\\+|~");

            for (String compound : compounds) {
                compound = compound.trim();
                if (compound.isEmpty()) continue;

                if (compound.startsWith("*")) {
                    continue;
                }

                Matcher matcher = TYPE_SELECTOR_PATTERN.matcher(compound);
                if (matcher.find()) {
                    String tag = matcher.group(2);
                    if (tag != null && !tag.isEmpty()) {
                        healingElementMetadata.addTagName(tag.toLowerCase());
                    }
                }
            }
        }

    }

    @Override
    public boolean supports(final Object locatorType) {
        return locatorType instanceof CssSelector;
    }
}
