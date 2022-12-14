
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
 *         &lt;element name="app" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="appPackage" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="appActivity" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="playMarket" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "androidDevice", propOrder = {
    "app",
    "appPackage",
    "appActivity"
})
public class AndroidDevice
    extends NativeDevice
{

    @XmlElement(required = true)
    protected String app;
    @XmlElement(required = true)
    protected String appPackage;
    @XmlElement(required = true)
    protected String appActivity;
    @XmlAttribute(name = "playMarket", required = true)
    protected boolean playMarket;

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
     * Gets the value of the playMarket property.
     *
     */
    public boolean isPlayMarketEnabled() {
        return playMarket;
    }

    /**
     * Sets the value of the playMarket property.
     *
     */
    public void setPlayMarketEnabled(boolean value) {
        this.playMarket = value;
    }
}
