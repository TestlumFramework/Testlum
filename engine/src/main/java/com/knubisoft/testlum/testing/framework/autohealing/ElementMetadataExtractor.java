package com.knubisoft.testlum.testing.framework.autohealing;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealingElementMetadata;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.SpecificMetadataExtractor;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.locator.GenericMetadataExtractor;
import com.knubisoft.testlum.testing.model.pages.Locator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ElementMetadataExtractor {

    private final List<SpecificMetadataExtractor> specificMetadataExtractors;
    private final GenericMetadataExtractor genericMetadataExtractor;

    public HealingElementMetadata extractMetadata(final Locator locator) {
        HealingElementMetadata healingElementMetadata = new HealingElementMetadata();

        if (locator.getXpathOrIdOrClassName() != null) {
            for (Object item : locator.getXpathOrIdOrClassName()) {
                for (SpecificMetadataExtractor extractor : specificMetadataExtractors) {
                    if (extractor.supports(item)) {
                        extractor.extractMetadata(item, healingElementMetadata);
                    }
                }
            }
        }

        genericMetadataExtractor.extract(locator.getLocatorId(), healingElementMetadata);

        return healingElementMetadata;
    }

}
