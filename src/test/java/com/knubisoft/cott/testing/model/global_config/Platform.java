
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for platform.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="platform"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="android"/&gt;
 *     &lt;enumeration value="ios"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "platform")
@XmlEnum
public enum Platform {

    @XmlEnumValue("android")
    ANDROID("android"),
    @XmlEnumValue("ios")
    IOS("ios");
    private final String value;

    Platform(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Platform fromValue(String v) {
        for (Platform c: Platform.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
