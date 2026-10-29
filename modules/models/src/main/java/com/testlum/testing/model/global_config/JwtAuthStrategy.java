
package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for jwtAuthStrategy complex type&lt;/p&gt;.
 *
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 *
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="jwtAuthStrategy"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/global-config}authStrategy"&gt;
 *       &lt;attribute name="loginEndpoint" use="required" type="{http://www.testlum.com/testing/model/global-config}url" /&gt;
 *       &lt;attribute name="tokenName" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString" default="token" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "jwtAuthStrategy")
public class JwtAuthStrategy
    extends AuthStrategy
{

    @XmlAttribute(name = "loginEndpoint", required = true)
    protected String loginEndpoint;
    @XmlAttribute(name = "tokenName")
    protected String tokenName;

    /**
     * Gets the value of the loginEndpoint property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLoginEndpoint() {
        return loginEndpoint;
    }

    /**
     * Sets the value of the loginEndpoint property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLoginEndpoint(String value) {
        this.loginEndpoint = value;
    }

    /**
     * Gets the value of the tokenName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTokenName() {
        if (tokenName == null) {
            return "token";
        } else {
            return tokenName;
        }
    }

    /**
     * Sets the value of the tokenName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTokenName(String value) {
        this.tokenName = value;
    }

}
