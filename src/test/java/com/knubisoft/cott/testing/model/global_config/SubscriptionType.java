
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for subscriptionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="subscriptionType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="stripe"/&gt;
 *     &lt;enumeration value="free"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "subscriptionType")
@XmlEnum
public enum SubscriptionType {

    @XmlEnumValue("stripe")
    STRIPE("stripe"),
    @XmlEnumValue("free")
    FREE("free");
    private final String value;

    SubscriptionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SubscriptionType fromValue(String v) {
        for (SubscriptionType c: SubscriptionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
