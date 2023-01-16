
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for swipeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="swipeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="element"/&gt;
 *     &lt;enumeration value="page"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "swipeType")
@XmlEnum
public enum SwipeType {

    @XmlEnumValue("element")
    ELEMENT("element"),
    @XmlEnumValue("page")
    PAGE("page");
    private final String value;

    SwipeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SwipeType fromValue(String v) {
        for (SwipeType c: SwipeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
