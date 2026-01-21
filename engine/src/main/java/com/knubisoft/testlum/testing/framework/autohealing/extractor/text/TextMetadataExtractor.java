package com.knubisoft.testlum.testing.framework.autohealing.extractor.text;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealingElementMetadata;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.SpecificMetadataExtractor;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.strategy.TextExtractor;
import com.knubisoft.testlum.testing.model.pages.Text;
import org.springframework.stereotype.Component;

@Component
public class TextMetadataExtractor implements SpecificMetadataExtractor<Text>, TextExtractor {

    @Override
    public void extractMetadata(final Text type, final HealingElementMetadata metadata) {
        this.extractText(type.getValue(), metadata);
    }

    @Override
    public boolean supports(final Object locatorType) {
        return locatorType instanceof Text;
    }

    @Override
    public void extractText(final String text, final HealingElementMetadata healingElementMetadata) {
        healingElementMetadata.addText(text);
    }
}
