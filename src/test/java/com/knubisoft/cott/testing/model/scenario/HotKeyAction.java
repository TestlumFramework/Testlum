
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for hotKeyAction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="hotKeyAction"&gt;
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
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "hotKeyAction")
@XmlEnum
public enum HotKeyAction {

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
    GO_TO_PREVIOUS_TAB("goToPreviousTab");
    private final String value;

    HotKeyAction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HotKeyAction fromValue(String v) {
        for (HotKeyAction c: HotKeyAction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
