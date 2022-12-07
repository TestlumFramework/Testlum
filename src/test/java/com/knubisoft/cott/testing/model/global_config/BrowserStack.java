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
 *         &lt;element name="deviceSettings" type="{http://www.knubisoft.com/cott/testing/model/global-config}playMarketLogin"/&gt;
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

    public BrowserStackLogin getBrowserStackLogin() {
        return browserStackLogin;
    }

    public void setBrowserStackLogin(BrowserStackLogin browserStackLogin) {
        this.browserStackLogin = browserStackLogin;
    }

    public PlayMarketLogin getPlayMarketLogin() {
        return playMarketLogin;
    }

    public void setPlayMarketLogin(PlayMarketLogin playMarketLogin) {
        this.playMarketLogin = playMarketLogin;
    }
}
