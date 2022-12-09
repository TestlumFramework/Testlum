package com.knubisoft.cott.testing.model.global_config;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for browserStack complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="browserStack"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="browserStackLogin" type="{http://www.knubisoft.com/cott/testing/model/global-config}browserStackLogin"/&gt;
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
@XmlType(name = "browserStack", propOrder = {
        "browserStackLogin",
        "playMarketLogin"
})
public class BrowserStack {

    @XmlElement(required = true)
    protected BrowserStackLogin browserStackLogin;
    @XmlElement(required = true)
    protected PlayMarketLogin playMarketLogin;

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
