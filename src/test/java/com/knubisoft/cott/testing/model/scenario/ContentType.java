
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contentType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="contentType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="text/plain"/&gt;
 *     &lt;enumeration value="image/jpeg"/&gt;
 *     &lt;enumeration value="application/json"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "contentType")
@XmlEnum
public enum ContentType {

    @XmlEnumValue("text/plain")
    TEXT_PLAIN("text/plain"),
    @XmlEnumValue("image/jpeg")
    IMAGE_JPEG("image/jpeg"),
    @XmlEnumValue("application/json")
    APPLICATION_JSON("application/json");
    private final String value;

    ContentType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ContentType fromValue(String v) {
        for (ContentType c: ContentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
