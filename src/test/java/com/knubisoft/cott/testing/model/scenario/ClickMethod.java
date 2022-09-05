
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for clickMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="clickMethod"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="selenium"/&gt;
 *     &lt;enumeration value="js"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "clickMethod")
@XmlEnum
public enum ClickMethod {

    @XmlEnumValue("selenium")
    SELENIUM("selenium"),
    @XmlEnumValue("js")
    JS("js");
    private final String value;

    ClickMethod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ClickMethod fromValue(String v) {
        for (ClickMethod c: ClickMethod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
