package com.knubisoft.testlum.testing.framework.autohealing.extractor.strategy;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealingElementMetadata;

public interface ClassExtractor {

    void extractClass(String value, HealingElementMetadata healingElementMetadata);

}
