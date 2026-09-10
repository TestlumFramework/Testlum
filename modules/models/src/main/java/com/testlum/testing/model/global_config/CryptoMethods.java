package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>Java class for cryptoMethods.</p>
 *
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="cryptoMethods">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="AES"/>
 *     <enumeration value="ECC"/>
 *     <enumeration value="CHACHA20"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 *
 */
@XmlType(name = "cryptoMethods")
@XmlEnum
public enum CryptoMethods {

    @XmlEnumValue("AES")
    AES("AES"),
    @XmlEnumValue("ECC")
    ECC("ECC"),
    @XmlEnumValue("CHACHA20")
    CHACHA20("CHACHA20");

    private final String value;

    CryptoMethods(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CryptoMethods fromValue(String v) {
        for (CryptoMethods c: CryptoMethods.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
