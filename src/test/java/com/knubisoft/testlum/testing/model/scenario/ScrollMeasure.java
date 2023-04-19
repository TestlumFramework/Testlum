
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for scrollMeasure.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="scrollMeasure"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="pixel"/&gt;
 *     &lt;enumeration value="percent"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "scrollMeasure")
@XmlEnum
public enum ScrollMeasure {

    @XmlEnumValue("pixel")
    PIXEL("pixel"),
    @XmlEnumValue("percent")
    PERCENT("percent");
    private final String value;

    ScrollMeasure(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ScrollMeasure fromValue(String v) {
        for (ScrollMeasure c: ScrollMeasure.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
