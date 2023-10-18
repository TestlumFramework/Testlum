
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for navigateNativeDestination.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="navigateNativeDestination"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="back"/&gt;
 *     &lt;enumeration value="home"/&gt;
 *     &lt;enumeration value="overview"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "navigateNativeDestination")
@XmlEnum
public enum NavigateNativeDestination {

    @XmlEnumValue("back")
    BACK("back"),
    @XmlEnumValue("home")
    HOME("home"),
    @XmlEnumValue("overview")
    OVERVIEW("overview");
    private final String value;

    NavigateNativeDestination(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NavigateNativeDestination fromValue(String v) {
        for (NavigateNativeDestination c: NavigateNativeDestination.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
