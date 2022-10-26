
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for singleKeyAction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="singleKeyAction"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="tab"/&gt;
 *     &lt;enumeration value="enter"/&gt;
 *     &lt;enumeration value="delete"/&gt;
 *     &lt;enumeration value="escape"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "singleKeyAction")
@XmlEnum
public enum SingleKeyAction {

    @XmlEnumValue("tab")
    TAB("tab"),
    @XmlEnumValue("enter")
    ENTER("enter"),
    @XmlEnumValue("delete")
    DELETE("delete"),
    @XmlEnumValue("escape")
    ESCAPE("escape");
    private final String value;

    SingleKeyAction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SingleKeyAction fromValue(String v) {
        for (SingleKeyAction c: SingleKeyAction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
