
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="web" type="{http://www.knubisoft.com/testlum/testing/model/global-config}web" minOccurs="0"/&gt;
 *         &lt;element name="mobilebrowser" type="{http://www.knubisoft.com/testlum/testing/model/global-config}mobilebrowser" minOccurs="0"/&gt;
 *         &lt;element name="native" type="{http://www.knubisoft.com/testlum/testing/model/global-config}native" minOccurs="0"/&gt;
 *         &lt;element name="browserStackLogin" type="{http://www.knubisoft.com/testlum/testing/model/global-config}browserStackLogin" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "web",
    "mobilebrowser",
    "_native",
    "browserStackLogin"
})
@XmlRootElement(name = "uiConfig")
public class UiConfig {

    protected Web web;
    protected Mobilebrowser mobilebrowser;
    @XmlElement(name = "native")
    protected Native _native;
    protected BrowserStackLogin browserStackLogin;

    /**
     * Gets the value of the web property.
     * 
     * @return
     *     possible object is
     *     {@link Web }
     *     
     */
    public Web getWeb() {
        return web;
    }

    /**
     * Sets the value of the web property.
     * 
     * @param value
     *     allowed object is
     *     {@link Web }
     *     
     */
    public void setWeb(Web value) {
        this.web = value;
    }

    /**
     * Gets the value of the mobilebrowser property.
     * 
     * @return
     *     possible object is
     *     {@link Mobilebrowser }
     *     
     */
    public Mobilebrowser getMobilebrowser() {
        return mobilebrowser;
    }

    /**
     * Sets the value of the mobilebrowser property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mobilebrowser }
     *     
     */
    public void setMobilebrowser(Mobilebrowser value) {
        this.mobilebrowser = value;
    }

    /**
     * Gets the value of the native property.
     * 
     * @return
     *     possible object is
     *     {@link Native }
     *     
     */
    public Native getNative() {
        return _native;
    }

    /**
     * Sets the value of the native property.
     * 
     * @param value
     *     allowed object is
     *     {@link Native }
     *     
     */
    public void setNative(Native value) {
        this._native = value;
    }

    /**
     * Gets the value of the browserStackLogin property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserStackLogin }
     *     
     */
    public BrowserStackLogin getBrowserStackLogin() {
        return browserStackLogin;
    }

    /**
     * Sets the value of the browserStackLogin property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserStackLogin }
     *     
     */
    public void setBrowserStackLogin(BrowserStackLogin value) {
        this.browserStackLogin = value;
    }

}
