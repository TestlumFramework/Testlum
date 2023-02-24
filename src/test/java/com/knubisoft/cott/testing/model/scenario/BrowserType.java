
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for browserType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="browserType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="webBrowser"/&gt;
 *     &lt;enumeration value="mobileBrowser"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "browserType")
@XmlEnum
public enum BrowserType {

    @XmlEnumValue("webBrowser")
    WEB_BROWSER("webBrowser"),
    @XmlEnumValue("mobileBrowser")
    MOBILE_BROWSER("mobileBrowser");
    private final String value;

    BrowserType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BrowserType fromValue(String v) {
        for (BrowserType c: BrowserType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
