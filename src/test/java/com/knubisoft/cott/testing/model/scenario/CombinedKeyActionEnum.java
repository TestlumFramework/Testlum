
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for combinedKeyActionEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="combinedKeyActionEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="highLightAll"/&gt;
 *     &lt;enumeration value="copy"/&gt;
 *     &lt;enumeration value="paste"/&gt;
 *     &lt;enumeration value="cut"/&gt;
 *     &lt;enumeration value="newTab"/&gt;
 *     &lt;enumeration value="newAnonymousTab"/&gt;
 *     &lt;enumeration value="openClosedTab"/&gt;
 *     &lt;enumeration value="openClosedTabs"/&gt;
 *     &lt;enumeration value="goToNextTab"/&gt;
 *     &lt;enumeration value="goToPreviousTab"/&gt;
 *     &lt;enumeration value="openDevTools"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "combinedKeyActionEnum")
@XmlEnum
public enum CombinedKeyActionEnum {

    @XmlEnumValue("highLightAll")
    HIGH_LIGHT_ALL("highLightAll"),
    @XmlEnumValue("copy")
    COPY("copy"),
    @XmlEnumValue("paste")
    PASTE("paste"),
    @XmlEnumValue("cut")
    CUT("cut"),
    @XmlEnumValue("newTab")
    NEW_TAB("newTab"),
    @XmlEnumValue("newAnonymousTab")
    NEW_ANONYMOUS_TAB("newAnonymousTab"),
    @XmlEnumValue("openClosedTab")
    OPEN_CLOSED_TAB("openClosedTab"),
    @XmlEnumValue("openClosedTabs")
    OPEN_CLOSED_TABS("openClosedTabs"),
    @XmlEnumValue("goToNextTab")
    GO_TO_NEXT_TAB("goToNextTab"),
    @XmlEnumValue("goToPreviousTab")
    GO_TO_PREVIOUS_TAB("goToPreviousTab"),
    @XmlEnumValue("openDevTools")
    OPEN_DEV_TOOLS("openDevTools");
    private final String value;

    CombinedKeyActionEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CombinedKeyActionEnum fromValue(String v) {
        for (CombinedKeyActionEnum c: CombinedKeyActionEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
