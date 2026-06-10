package com.knubisoft.testlum.testing.framework.autohealing.extractor.locator;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealingElementMetadata;

public interface GenericMetadataExtractor {

    void extract(String value, HealingElementMetadata metadata);

}
