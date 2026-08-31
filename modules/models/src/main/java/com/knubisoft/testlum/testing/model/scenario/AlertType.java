
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

/**
 * <p>Java class for alertType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="alertType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="alert"/&gt;
 *     &lt;enumeration value="prompt"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "alertType")
@XmlEnum
public enum AlertType {

    @XmlEnumValue("alert")
    ALERT("alert"),
    @XmlEnumValue("prompt")
    PROMPT("prompt");
    private final String value;

    AlertType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AlertType fromValue(String v) {
        for (AlertType c: AlertType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
