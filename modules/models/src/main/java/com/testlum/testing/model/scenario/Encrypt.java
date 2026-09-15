package com.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "encrypt")
public class Encrypt extends CryptoOperation {
    @Override
    public String getAction() {
        return "ENCRYPT";
    }
}
