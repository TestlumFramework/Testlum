
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for autoHealingMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="autoHealingMode"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="soft"/&gt;
 *     &lt;enumeration value="persistent"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "autoHealingMode")
@XmlEnum
public enum AutoHealingMode {

    @XmlEnumValue("soft")
    SOFT("soft"),
    @XmlEnumValue("persistent")
    PERSISTENT("persistent");
    private final String value;

    AutoHealingMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AutoHealingMode fromValue(String v) {
        for (AutoHealingMode c: AutoHealingMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
