package com.testlum.testing.framework.autohealing.extractor.strategy;

import com.testlum.testing.framework.autohealing.dto.HealingElementMetadata;

public interface TextExtractor {

    void extractText(String value, HealingElementMetadata healingElementMetadata);

}
