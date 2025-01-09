
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="mode"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="strict"/&gt;
 *     &lt;enumeration value="lenient"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "mode")
@XmlEnum
public enum Mode {

    @XmlEnumValue("strict")
    STRICT("strict"),
    @XmlEnumValue("lenient")
    LENIENT("lenient");
    private final String value;

    Mode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Mode fromValue(String v) {
        for (Mode c: Mode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
