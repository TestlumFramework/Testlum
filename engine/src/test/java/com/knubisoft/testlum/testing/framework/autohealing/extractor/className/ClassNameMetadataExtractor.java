package com.knubisoft.testlum.testing.framework.autohealing.extractor.className;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealingElementMetadata;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.strategy.ClassExtractor;
import com.knubisoft.testlum.testing.framework.autohealing.extractor.SpecificMetadataExtractor;
import com.knubisoft.testlum.testing.model.pages.ClassName;
import org.springframework.stereotype.Component;

@Component
public class ClassNameMetadataExtractor implements SpecificMetadataExtractor<ClassName>, ClassExtractor {

    @Override
    public void extractMetadata(final ClassName type, final HealingElementMetadata metadata) {
        this.extractClass(type.getValue(), metadata);
    }

    @Override
    public void extractClass(final String className, final HealingElementMetadata healingElementMetadata) {
        healingElementMetadata.addClass(className);
    }

    @Override
    public boolean supports(final Object locatorType) {
        return locatorType instanceof ClassName;
    }
}
