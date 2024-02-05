
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for typeForAllValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="typeForAllValues"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="deselect"/&gt;
 *     &lt;enumeration value="select"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "typeForAllValues")
@XmlEnum
public enum TypeForAllValues {

    @XmlEnumValue("deselect")
    DESELECT("deselect"),
    @XmlEnumValue("select")
    SELECT("select");
    private final String value;

    TypeForAllValues(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TypeForAllValues fromValue(String v) {
        for (TypeForAllValues c: TypeForAllValues.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
