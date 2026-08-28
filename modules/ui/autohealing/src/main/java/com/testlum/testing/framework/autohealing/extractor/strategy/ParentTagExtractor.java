package com.testlum.testing.framework.autohealing.extractor.strategy;

import com.testlum.testing.framework.autohealing.dto.HealingElementMetadata;

public interface ParentTagExtractor {

    void extractParentTag(String value, HealingElementMetadata healingElementMetadata);

}
