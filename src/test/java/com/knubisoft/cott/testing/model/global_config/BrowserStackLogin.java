
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for browserStackLogin complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="browserStackLogin"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="password" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="playMarketLogin" type="{http://www.knubisoft.com/cott/testing/model/global-config}playMarketLogin"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "browserStackLogin", propOrder = {
    "username",
    "password",
    "playMarketLogin"
})
public class BrowserStackLogin {

    @XmlElement(required = true)
    protected String username;
    @XmlElement(required = true)
    protected String password;
    @XmlElement(required = true)
    protected PlayMarketLogin playMarketLogin;

    /**
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the playMarketLogin property.
     * 
     * @return
     *     possible object is
     *     {@link PlayMarketLogin }
     *     
     */
    public PlayMarketLogin getPlayMarketLogin() {
        return playMarketLogin;
    }

    /**
     * Sets the value of the playMarketLogin property.
     * 
     * @param value
     *     allowed object is
     *     {@link PlayMarketLogin }
     *     
     */
    public void setPlayMarketLogin(PlayMarketLogin value) {
        this.playMarketLogin = value;
    }

}
