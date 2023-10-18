
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for websocketProtocol.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="websocketProtocol"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="standard"/&gt;
 *     &lt;enumeration value="stomp"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "websocketProtocol")
@XmlEnum
public enum WebsocketProtocol {

    @XmlEnumValue("standard")
    STANDARD("standard"),
    @XmlEnumValue("stomp")
    STOMP("stomp");
    private final String value;

    WebsocketProtocol(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static WebsocketProtocol fromValue(String v) {
        for (WebsocketProtocol c: WebsocketProtocol.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
