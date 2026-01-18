
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dateUnit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="dateUnit"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="minutes"/&gt;
 *     &lt;enumeration value="seconds"/&gt;
 *     &lt;enumeration value="hours"/&gt;
 *     &lt;enumeration value="days"/&gt;
 *     &lt;enumeration value="months"/&gt;
 *     &lt;enumeration value="years"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "dateUnit")
@XmlEnum
public enum DateUnit {

    @XmlEnumValue("minutes")
    MINUTES("minutes"),
    @XmlEnumValue("seconds")
    SECONDS("seconds"),
    @XmlEnumValue("hours")
    HOURS("hours"),
    @XmlEnumValue("days")
    DAYS("days"),
    @XmlEnumValue("months")
    MONTHS("months"),
    @XmlEnumValue("years")
    YEARS("years");
    private final String value;

    DateUnit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DateUnit fromValue(String v) {
        for (DateUnit c: DateUnit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
