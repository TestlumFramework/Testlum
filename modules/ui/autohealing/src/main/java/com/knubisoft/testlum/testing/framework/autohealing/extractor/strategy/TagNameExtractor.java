package com.knubisoft.testlum.testing.framework.autohealing.extractor.strategy;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealingElementMetadata;

public interface TagNameExtractor {

    void extractTagName(String value, HealingElementMetadata healingElementMetadata);

}
