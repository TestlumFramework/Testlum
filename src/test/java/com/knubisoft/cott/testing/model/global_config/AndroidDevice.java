
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for androidDevice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="androidDevice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/global-config}nativeDevice"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="appPackage" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString" minOccurs="0"/&gt;
 *         &lt;element name="appActivity" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString" minOccurs="0"/&gt;
 *         &lt;element name="app" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString" minOccurs="0"/&gt;
 *         &lt;element name="playMarketLogin" type="{http://www.knubisoft.com/cott/testing/model/global-config}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "androidDevice", propOrder = {
    "appPackage",
    "appActivity",
    "app"
})
public class AndroidDevice
    extends NativeDevice
{

    @XmlElement(required = true)
    protected String appPackage;
    @XmlElement(required = true)
    protected String appActivity;
    @XmlElement(required = true)
    protected String app;
    @XmlAttribute(name = "playMarketLogin", required = true)
    protected boolean playMarketLoginEnabled;

    /**
     * Gets the value of the appPackage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppPackage() {
        return appPackage;
    }

    /**
     * Sets the value of the appPackage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppPackage(String value) {
        this.appPackage = value;
    }

    /**
     * Gets the value of the appActivity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppActivity() {
        return appActivity;
    }

    /**
     * Sets the value of the appActivity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppActivity(String value) {
        this.appActivity = value;
    }

    /**
     * Gets the value of the app property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getApp() {
        return app;
    }

    /**
     * Sets the value of the app property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setApp(String value) {
        this.app = value;
    }

    /**
     * Gets the value of the playMarketLoginEnabled property.
     */
    public boolean isPlayMarketLoginEnabled() {
        return playMarketLoginEnabled;
    }

    /**
     * Sets the value of the playMarketLoginEnabled property.
     */
    public void setPlayMarketLoginEnabled(boolean value) {
        this.playMarketLoginEnabled = value;
    }
}
