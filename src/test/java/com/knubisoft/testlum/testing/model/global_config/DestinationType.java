
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for destinationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="destinationType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="queue"/&gt;
 *     &lt;enumeration value="exchange"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "destinationType")
@XmlEnum
public enum DestinationType {

    @XmlEnumValue("queue")
    QUEUE("queue"),
    @XmlEnumValue("exchange")
    EXCHANGE("exchange");
    private final String value;

    DestinationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DestinationType fromValue(String v) {
        for (DestinationType c: DestinationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
