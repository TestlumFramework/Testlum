
package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * &lt;p&gt;Java class for emailProtocol&lt;/p&gt;.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 * &lt;pre&gt;{&#064;code
 * &lt;simpleType name="emailProtocol"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="imaps"/&gt;
 *     &lt;enumeration value="imap"/&gt;
 *     &lt;enumeration value="pop3s"/&gt;
 *     &lt;enumeration value="pop3"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * }&lt;/pre&gt;
 * 
 */
@XmlType(name = "emailProtocol")
@XmlEnum
public enum EmailProtocol {

    @XmlEnumValue("imaps")
    IMAPS("imaps"),
    @XmlEnumValue("imap")
    IMAP("imap"),
    @XmlEnumValue("pop3s")
    POP_3_S("pop3s"),
    @XmlEnumValue("pop3")
    POP_3("pop3");
    private final String value;

    EmailProtocol(String v) {
        value = v;
    }

    /**
     * Gets the value associated to the enum constant.
     * 
     * @return
     *     The value linked to the enum.
     */
    public String value() {
        return value;
    }

    /**
     * Gets the enum associated to the value passed as parameter.
     * 
     * @param v
     *     The value to get the enum from.
     * @return
     *     The enum which corresponds to the value, if it exists.
     * @throws IllegalArgumentException
     *     If no value matches in the enum declaration.
     */
    public static EmailProtocol fromValue(String v) {
        for (EmailProtocol c: EmailProtocol.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
