
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for auth complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="auth"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="autoLogout" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="authStrategy" type="{http://www.knubisoft.com/testlum/testing/model/global-config}authStrategies" default="default" /&gt;
 *       &lt;attribute name="tokenName" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString" default="token" /&gt;
 *       &lt;attribute name="authCustomClassName" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "auth")
public class Auth {

    @XmlAttribute(name = "autoLogout")
    protected Boolean autoLogout;
    @XmlAttribute(name = "authStrategy")
    protected AuthStrategies authStrategy;
    @XmlAttribute(name = "tokenName")
    protected String tokenName;
    @XmlAttribute(name = "authCustomClassName")
    protected String authCustomClassName;

    /**
     * Gets the value of the autoLogout property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoLogout() {
        if (autoLogout == null) {
            return true;
        } else {
            return autoLogout;
        }
    }

    /**
     * Sets the value of the autoLogout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoLogout(Boolean value) {
        this.autoLogout = value;
    }

    /**
     * Gets the value of the authStrategy property.
     * 
     * @return
     *     possible object is
     *     {@link AuthStrategies }
     *     
     */
    public AuthStrategies getAuthStrategy() {
        if (authStrategy == null) {
            return AuthStrategies.DEFAULT;
        } else {
            return authStrategy;
        }
    }

    /**
     * Sets the value of the authStrategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthStrategies }
     *     
     */
    public void setAuthStrategy(AuthStrategies value) {
        this.authStrategy = value;
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

    /**
     * Gets the value of the authCustomClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthCustomClassName() {
        return authCustomClassName;
    }

    /**
     * Sets the value of the authCustomClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthCustomClassName(String value) {
        this.authCustomClassName = value;
    }

}
