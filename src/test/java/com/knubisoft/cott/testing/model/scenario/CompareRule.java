
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for compareRule.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="compareRule"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="equals"/&gt;
 *     &lt;enumeration value="contains"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "compareRule")
@XmlEnum
public enum CompareRule {

    @XmlEnumValue("equals")
    EQUALS("equals"),
    @XmlEnumValue("contains")
    CONTAINS("contains");
    private final String value;

    CompareRule(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CompareRule fromValue(String v) {
        for (CompareRule c: CompareRule.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
