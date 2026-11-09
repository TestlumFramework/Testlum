
package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for googleAuthStrategy complex type&lt;/p&gt;.
 *
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 *
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="googleAuthStrategy"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/global-config}authStrategy"&gt;
 *       &lt;attribute name="clientId" use="required" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString" /&gt;
 *       &lt;attribute name="clientSecret" use="required" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "googleAuthStrategy")
public class GoogleAuthStrategy
    extends AuthStrategy
{

    @XmlAttribute(name = "clientId", required = true)
    protected String clientId;
    @XmlAttribute(name = "clientSecret", required = true)
    protected String clientSecret;

    /**
     * Gets the value of the clientId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the value of the clientId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setClientId(String value) {
        this.clientId = value;
    }

    /**
     * Gets the value of the clientSecret property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Sets the value of the clientSecret property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setClientSecret(String value) {
        this.clientSecret = value;
    }

}
