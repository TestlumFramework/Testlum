
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for timeunit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="timeunit"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="millis"/&gt;
 *     &lt;enumeration value="seconds"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "timeunit")
@XmlEnum
public enum Timeunit {

    @XmlEnumValue("millis")
    MILLIS("millis"),
    @XmlEnumValue("seconds")
    SECONDS("seconds");
    private final String value;

    Timeunit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Timeunit fromValue(String v) {
        for (Timeunit c: Timeunit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
