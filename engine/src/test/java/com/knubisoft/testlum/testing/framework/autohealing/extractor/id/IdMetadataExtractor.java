package com.knubisoft.testlum.testing.framework.autohealing.extractor.id;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealingElementMetadata;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.strategy.IdExtractor;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.SpecificMetadataExtractor;
import com.knubisoft.testlum.testing.model.pages.Id;
import org.springframework.stereotype.Component;

@Component
public class IdMetadataExtractor implements SpecificMetadataExtractor<Id>, IdExtractor {

    @Override
    public void extractMetadata(final Id type, final HealingElementMetadata metadata) {
        this.extractId(type.getValue(), metadata);
    }

    @Override
    public void extractId(final String id, final HealingElementMetadata healingElementMetadata) {
        healingElementMetadata.addId(id);
    }

    @Override
    public boolean supports(final Object locatorType) {
        return locatorType instanceof Id;
    }
}
