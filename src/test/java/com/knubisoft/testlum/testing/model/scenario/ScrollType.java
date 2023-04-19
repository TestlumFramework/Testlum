
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for scrollType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="scrollType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="inner"/&gt;
 *     &lt;enumeration value="page"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "scrollType")
@XmlEnum
public enum ScrollType {

    @XmlEnumValue("inner")
    INNER("inner"),
    @XmlEnumValue("page")
    PAGE("page");
    private final String value;

    ScrollType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ScrollType fromValue(String v) {
        for (ScrollType c: ScrollType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
