
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for navigateCommand.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="navigateCommand"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="back"/&gt;
 *     &lt;enumeration value="reload"/&gt;
 *     &lt;enumeration value="to"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "navigateCommand")
@XmlEnum
public enum NavigateCommand {

    @XmlEnumValue("back")
    BACK("back"),
    @XmlEnumValue("reload")
    RELOAD("reload"),
    @XmlEnumValue("to")
    TO("to");
    private final String value;

    NavigateCommand(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NavigateCommand fromValue(String v) {
        for (NavigateCommand c: NavigateCommand.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
