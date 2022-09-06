
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for attribute.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="attribute"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="outerHTML"/&gt;
 *     &lt;enumeration value="class"/&gt;
 *     &lt;enumeration value="id"/&gt;
 *     &lt;enumeration value="xpath"/&gt;
 *     &lt;enumeration value="cssSelector"/&gt;
 *     &lt;enumeration value="linkText"/&gt;
 *     &lt;enumeration value="partialLinkText"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "attribute")
@XmlEnum
public enum Attribute {

    @XmlEnumValue("outerHTML")
    OUTER_HTML("outerHTML"),
    @XmlEnumValue("class")
    CLASS("class"),
    @XmlEnumValue("id")
    ID("id"),
    @XmlEnumValue("xpath")
    XPATH("xpath"),
    @XmlEnumValue("cssSelector")
    CSS_SELECTOR("cssSelector"),
    @XmlEnumValue("linkText")
    LINK_TEXT("linkText"),
    @XmlEnumValue("partialLinkText")
    PARTIAL_LINK_TEXT("partialLinkText");
    private final String value;

    Attribute(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Attribute fromValue(String v) {
        for (Attribute c: Attribute.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
