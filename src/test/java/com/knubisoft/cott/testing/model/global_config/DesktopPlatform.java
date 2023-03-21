
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for desktopPlatform.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="desktopPlatform"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="windows"/&gt;
 *     &lt;enumeration value="macos"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "desktopPlatform")
@XmlEnum
public enum DesktopPlatform {

    @XmlEnumValue("windows")
    WINDOWS("windows"),
    @XmlEnumValue("macos")
    MACOS("macos");
    private final String value;

    DesktopPlatform(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DesktopPlatform fromValue(String v) {
        for (DesktopPlatform c: DesktopPlatform.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
