
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for authStrategies.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="authStrategies"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="basic"/&gt;
 *     &lt;enumeration value="jwt"/&gt;
 *     &lt;enumeration value="custom"/&gt;
 *     &lt;enumeration value="default"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "authStrategies")
@XmlEnum
public enum AuthStrategies {

    @XmlEnumValue("basic")
    BASIC("basic"),
    @XmlEnumValue("jwt")
    JWT("jwt"),
    @XmlEnumValue("custom")
    CUSTOM("custom"),
    @XmlEnumValue("default")
    DEFAULT("default");
    private final String value;

    AuthStrategies(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AuthStrategies fromValue(String v) {
        for (AuthStrategies c: AuthStrategies.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
