package com.knubisoft.testlum.testing.framework.autohealing.extractor.xpath;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealingElementMetadata;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.*;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.strategy.*;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.util.GenericAttributeMetadataExtractor;
import com.knubisoft.testlum.testing.model.pages.Xpath;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class XpathMetadataExtractor implements SpecificMetadataExtractor<Xpath>, AttributeExtractor,
        IdExtractor, TagNameExtractor, TextExtractor, AncestorIdExtractor, ParentTagExtractor {

    @Override
    public void extractMetadata(final Xpath type, final HealingElementMetadata metadata) {
        this.extractAttributes(type.getValue(), metadata);
        this.extractId(type.getValue(), metadata);
        this.extractTagName(type.getValue(), metadata);
        this.extractText(type.getValue(), metadata);
        this.extractAncestorId(type.getValue(), metadata);
        this.extractParentTag(type.getValue(), metadata);
    }

    @Override
    public void extractAttributes(final String xpath, final HealingElementMetadata healingElementMetadata) {
        Map<String, String> attributes = GenericAttributeMetadataExtractor.extractAttributes(xpath);
        healingElementMetadata.addAttributes(attributes);
    }

    @Override
    public void extractId(final String xpath, final HealingElementMetadata healingElementMetadata) {
        String regex = "@id='([^']+)'](?![^/]*?/)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(xpath);

        if (matcher.find()) {
            healingElementMetadata.addId(matcher.group(1));
        }
    }

    @Override
    public void extractTagName(final String xpath, final HealingElementMetadata healingElementMetadata) {
        Pattern pattern = Pattern.compile("(?://|/|\\./)([a-zA-Z0-9*]+)(?=\\[|/|\\)|$)");
        Matcher matcher = pattern.matcher(xpath);

        String possibleTagName = null;
        while (matcher.find()) {
            possibleTagName = matcher.group(1);
        }
        if (possibleTagName != null) {
            healingElementMetadata.addTagName(possibleTagName);
        }
    }

    @Override
    public void extractText(final String xpath, final HealingElementMetadata healingElementMetadata) {
        Pattern textPattern = Pattern.compile("(?:text\\(\\)|\\.)\\s*[=,]\\s*['\"]([^'\"]+)['\"]");
        Matcher textMatcher = textPattern.matcher(xpath);

        if (textMatcher.find()) {
            healingElementMetadata.addText(textMatcher.group(1));
        }
    }

    @Override
    public boolean supports(final Object locatorType) {
        return locatorType instanceof Xpath;
    }

    @Override
    public void extractAncestorId(final String xpath, final HealingElementMetadata healingElementMetadata) {
        Pattern idPattern = Pattern.compile("@id='([^']+)'");
        Matcher idMatcher = idPattern.matcher(xpath);
        while (idMatcher.find()) {
            healingElementMetadata.addAncestorId(idMatcher.group(1));
        }
    }

    @Override
    public void extractParentTag(final String xpath, final HealingElementMetadata healingElementMetadata) {
        Pattern hierarchyPattern = Pattern.compile("/([a-zA-Z0-9]+)/[a-zA-Z0-9]+(?:\\[|$)");
        Matcher hierarchyMatcher = hierarchyPattern.matcher(xpath);
        if (hierarchyMatcher.find()) {
            healingElementMetadata.addParentTag(hierarchyMatcher.group(1));
        }
    }
}
