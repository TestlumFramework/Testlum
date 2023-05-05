
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for exchangeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="exchangeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="direct"/&gt;
 *     &lt;enumeration value="topic"/&gt;
 *     &lt;enumeration value="fanout"/&gt;
 *     &lt;enumeration value="headers"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "exchangeType")
@XmlEnum
public enum ExchangeType {

    @XmlEnumValue("direct")
    DIRECT("direct"),
    @XmlEnumValue("topic")
    TOPIC("topic"),
    @XmlEnumValue("fanout")
    FANOUT("fanout"),
    @XmlEnumValue("headers")
    HEADERS("headers");
    private final String value;

    ExchangeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ExchangeType fromValue(String v) {
        for (ExchangeType c: ExchangeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
