
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

/**
 * <p>Java class for alertAction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="alertAction"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="accept"/&gt;
 *     &lt;enumeration value="dismiss"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "alertAction")
@XmlEnum
public enum AlertAction {

    @XmlEnumValue("accept")
    ACCEPT("accept"),
    @XmlEnumValue("dismiss")
    DISMISS("dismiss");
    private final String value;

    AlertAction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AlertAction fromValue(String v) {
        for (AlertAction c: AlertAction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
