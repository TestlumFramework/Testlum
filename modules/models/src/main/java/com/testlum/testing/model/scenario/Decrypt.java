package com.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "decrypt")
public class Decrypt extends CryptoOperation {
    @Override
    public String getAction() {
        return "DECRYPT";
    }
}
