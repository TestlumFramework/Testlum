
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for singleKeyActionEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="singleKeyActionEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="tab"/&gt;
 *     &lt;enumeration value="enter"/&gt;
 *     &lt;enumeration value="delete"/&gt;
 *     &lt;enumeration value="escape"/&gt;
 *     &lt;enumeration value="space"/&gt;
 *     &lt;enumeration value="arrowLeft"/&gt;
 *     &lt;enumeration value="arrowRight"/&gt;
 *     &lt;enumeration value="arrowUp"/&gt;
 *     &lt;enumeration value="arrowDown"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "singleKeyActionEnum")
@XmlEnum
public enum SingleKeyActionEnum {

    @XmlEnumValue("tab")
    TAB("tab"),
    @XmlEnumValue("enter")
    ENTER("enter"),
    @XmlEnumValue("delete")
    DELETE("delete"),
    @XmlEnumValue("escape")
    ESCAPE("escape"),
    @XmlEnumValue("space")
    SPACE("space"),
    @XmlEnumValue("arrowLeft")
    ARROW_LEFT("arrowLeft"),
    @XmlEnumValue("arrowRight")
    ARROW_RIGHT("arrowRight"),
    @XmlEnumValue("arrowUp")
    ARROW_UP("arrowUp"),
    @XmlEnumValue("arrowDown")
    ARROW_DOWN("arrowDown");
    private final String value;

    SingleKeyActionEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SingleKeyActionEnum fromValue(String v) {
        for (SingleKeyActionEnum c: SingleKeyActionEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
