package com.testlum.testing.framework.autohealing.extractor.strategy;

import com.testlum.testing.framework.autohealing.dto.HealingElementMetadata;

public interface AttributeExtractor {

    void extractAttributes(String value, HealingElementMetadata healingElementMetadata);

}
