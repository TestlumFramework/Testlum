
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for varType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="varType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="value"/&gt;
 *     &lt;enumeration value="jpath"/&gt;
 *     &lt;enumeration value="expression"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "varType")
@XmlEnum
public enum VarType {

    @XmlEnumValue("value")
    VALUE("value"),
    @XmlEnumValue("jpath")
    JPATH("jpath"),
    @XmlEnumValue("expression")
    EXPRESSION("expression");
    private final String value;

    VarType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VarType fromValue(String v) {
        for (VarType c: VarType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
