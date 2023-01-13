
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for swipeElementDirection.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="swipeElementDirection"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="right"/&gt;
 *     &lt;enumeration value="left"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "swipeElementDirection")
@XmlEnum
public enum SwipeElementDirection {

    @XmlEnumValue("right")
    RIGHT("right"),
    @XmlEnumValue("left")
    LEFT("left");
    private final String value;

    SwipeElementDirection(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SwipeElementDirection fromValue(String v) {
        for (SwipeElementDirection c: SwipeElementDirection.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
