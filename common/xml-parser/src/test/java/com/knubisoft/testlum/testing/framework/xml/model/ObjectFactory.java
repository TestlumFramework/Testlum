package com.knubisoft.testlum.testing.framework.xml.model;

import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    public TestItem createTestItem() {
        return new TestItem();
    }
}
