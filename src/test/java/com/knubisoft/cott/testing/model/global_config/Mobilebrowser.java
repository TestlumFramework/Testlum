
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mobilebrowser complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mobilebrowser"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="baseUrl" type="{http://www.knubisoft.com/cott/testing/model/global-config}url"/&gt;
 *         &lt;element name="appiumServerUrl" type="{http://www.knubisoft.com/cott/testing/model/global-config}url"/&gt;
 *         &lt;element name="deviceSettings" type="{http://www.knubisoft.com/cott/testing/model/global-config}mobilebrowserDeviceSettings"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="browserStackConnection" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mobilebrowser", propOrder = {
    "baseUrl",
    "appiumServerUrl",
    "deviceSettings"
})
public class Mobilebrowser {

    @XmlElement(required = true)
    protected String baseUrl;
    @XmlElement(required = true)
    protected String appiumServerUrl;
    @XmlElement(required = true)
    protected MobilebrowserDeviceSettings deviceSettings;
    @XmlAttribute(name = "enabled", required = true)
    protected boolean enabled;
    @XmlAttribute(name = "browserStackConnection", required = true)
    protected boolean browserStackConnection;

    /**
     * Gets the value of the baseUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the value of the baseUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBaseUrl(String value) {
        this.baseUrl = value;
    }

    /**
     * Gets the value of the appiumServerUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppiumServerUrl() {
        return appiumServerUrl;
    }

    /**
     * Sets the value of the appiumServerUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppiumServerUrl(String value) {
        this.appiumServerUrl = value;
    }

    /**
     * Gets the value of the deviceSettings property.
     * 
     * @return
     *     possible object is
     *     {@link MobilebrowserDeviceSettings }
     *     
     */
    public MobilebrowserDeviceSettings getDeviceSettings() {
        return deviceSettings;
    }

    /**
     * Sets the value of the deviceSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link MobilebrowserDeviceSettings }
     *     
     */
    public void setDeviceSettings(MobilebrowserDeviceSettings value) {
        this.deviceSettings = value;
    }

    /**
     * Gets the value of the enabled property.
     * 
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     * 
     */
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the browserStackConnection property.
     *
     */
    public boolean isBrowserStackEnabled() {
        return browserStackConnection;
    }

    /**
     * Sets the value of the browserStackConnection property.
     *
     */
    public void setBrowserStackConnection(boolean browserStackConnection) {
        this.browserStackConnection = browserStackConnection;
    }
}
