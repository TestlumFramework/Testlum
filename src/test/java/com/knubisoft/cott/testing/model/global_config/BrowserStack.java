package com.knubisoft.cott.testing.model.global_config;


import javax.xml.bind.annotation.*;

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
 *         &lt;element name="appiumServerUrl" type="{http://www.knubisoft.com/cott/testing/model/global-config}url"/&gt;
 *         &lt;element name="deviceSettings" type="{http://www.knubisoft.com/cott/testing/model/global-config}nativeDeviceSettings"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
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
        "playMarketLogin",
        "localTesting"
})
public class BrowserStack {

    @XmlElement(required = true)
    protected BrowserStackLogin browserStackLogin;
    @XmlElement(required = true)
    protected PlayMarketLogin playMarketLogin;
    @XmlAttribute(name="localTesting", required = true)
    protected boolean localTestingEnabled;

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

    public boolean isLocalTestingEnabled() {
        return localTestingEnabled;
    }

    public void setLocalTestingEnabled(boolean localTestingEnabled) {
        this.localTestingEnabled = localTestingEnabled;
    }
}
