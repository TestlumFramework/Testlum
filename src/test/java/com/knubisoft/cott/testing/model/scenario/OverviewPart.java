
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for overviewPart.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="overviewPart"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="Description"/&gt;
 *     &lt;enumeration value="Name"/&gt;
 *     &lt;enumeration value="Jira"/&gt;
 *     &lt;enumeration value="Developer"/&gt;
 *     &lt;enumeration value="Link"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "overviewPart")
@XmlEnum
public enum OverviewPart {

    @XmlEnumValue("Description")
    DESCRIPTION("Description"),
    @XmlEnumValue("Name")
    NAME("Name"),
    @XmlEnumValue("Jira")
    JIRA("Jira"),
    @XmlEnumValue("Developer")
    DEVELOPER("Developer"),
    @XmlEnumValue("Link")
    LINK("Link");
    private final String value;

    OverviewPart(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OverviewPart fromValue(String v) {
        for (OverviewPart c: OverviewPart.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
