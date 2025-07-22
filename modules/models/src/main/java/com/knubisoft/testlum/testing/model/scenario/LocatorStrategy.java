
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for locatorStrategy.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="locatorStrategy"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="locatorId"/&gt;
 *     &lt;enumeration value="xpath"/&gt;
 *     &lt;enumeration value="id"/&gt;
 *     &lt;enumeration value="class"/&gt;
 *     &lt;enumeration value="cssSelector"/&gt;
 *     &lt;enumeration value="text"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "locatorStrategy")
@XmlEnum
public enum LocatorStrategy {

    @XmlEnumValue("locatorId")
    LOCATOR_ID("locatorId"),
    @XmlEnumValue("xpath")
    XPATH("xpath"),
    @XmlEnumValue("id")
    ID("id"),
    @XmlEnumValue("class")
    CLASS("class"),
    @XmlEnumValue("cssSelector")
    CSS_SELECTOR("cssSelector"),
    @XmlEnumValue("text")
    TEXT("text");
    private final String value;

    LocatorStrategy(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LocatorStrategy fromValue(String v) {
        for (LocatorStrategy c: LocatorStrategy.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
