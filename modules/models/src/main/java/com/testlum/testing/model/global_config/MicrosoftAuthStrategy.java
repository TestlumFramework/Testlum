
package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for microsoftAuthStrategy complex type&lt;/p&gt;.
 *
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 *
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="microsoftAuthStrategy"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/global-config}authStrategy"&gt;
 *       &lt;attribute name="clientId" use="required" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString" /&gt;
 *       &lt;attribute name="clientSecret" use="required" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString" /&gt;
 *       &lt;attribute name="tenant" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString" default="common" /&gt;
 *       &lt;attribute name="scope" use="required" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "microsoftAuthStrategy")
public class MicrosoftAuthStrategy
    extends AuthStrategy
{

    @XmlAttribute(name = "clientId", required = true)
    protected String clientId;
    @XmlAttribute(name = "clientSecret", required = true)
    protected String clientSecret;
    @XmlAttribute(name = "tenant")
    protected String tenant;
    @XmlAttribute(name = "scope", required = true)
    protected String scope;

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

    /**
     * Gets the value of the tenant property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTenant() {
        if (tenant == null) {
            return "common";
        } else {
            return tenant;
        }
    }

    /**
     * Sets the value of the tenant property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTenant(String value) {
        this.tenant = value;
    }

    /**
     * Gets the value of the scope property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setScope(String value) {
        this.scope = value;
    }

}
