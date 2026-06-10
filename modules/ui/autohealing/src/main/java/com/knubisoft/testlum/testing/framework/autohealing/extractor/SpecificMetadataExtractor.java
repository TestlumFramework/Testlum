package com.knubisoft.testlum.testing.framework.autohealing.extractor;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealingElementMetadata;

public interface SpecificMetadataExtractor<T> {

    void extractMetadata(T type, HealingElementMetadata metadata);

    boolean supports(Object locatorType);

}
