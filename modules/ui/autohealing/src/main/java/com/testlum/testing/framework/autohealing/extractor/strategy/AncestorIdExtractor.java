package com.testlum.testing.framework.autohealing.extractor.strategy;

import com.testlum.testing.framework.autohealing.dto.HealingElementMetadata;

public interface AncestorIdExtractor {

    void extractAncestorId(String value, HealingElementMetadata healingElementMetadata);

}
