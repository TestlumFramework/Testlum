
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for selectOrDeselectBy.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="selectOrDeselectBy"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="text"/&gt;
 *     &lt;enumeration value="value"/&gt;
 *     &lt;enumeration value="index"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "selectOrDeselectBy")
@XmlEnum
public enum SelectOrDeselectBy {

    @XmlEnumValue("text")
    TEXT("text"),
    @XmlEnumValue("value")
    VALUE("value"),
    @XmlEnumValue("index")
    INDEX("index");
    private final String value;

    SelectOrDeselectBy(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SelectOrDeselectBy fromValue(String v) {
        for (SelectOrDeselectBy c: SelectOrDeselectBy.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
