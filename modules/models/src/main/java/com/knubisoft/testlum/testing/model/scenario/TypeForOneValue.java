
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for typeForOneValue.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="typeForOneValue"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="select"/&gt;
 *     &lt;enumeration value="deselect"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "typeForOneValue")
@XmlEnum
public enum TypeForOneValue {

    @XmlEnumValue("select")
    SELECT("select"),
    @XmlEnumValue("deselect")
    DESELECT("deselect");
    private final String value;

    TypeForOneValue(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TypeForOneValue fromValue(String v) {
        for (TypeForOneValue c: TypeForOneValue.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
