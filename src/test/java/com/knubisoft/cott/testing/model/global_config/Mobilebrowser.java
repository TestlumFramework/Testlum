
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
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/global-config}settings"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="baseUrl" type="{http://www.knubisoft.com/cott/testing/model/global-config}url"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="appiumServer" type="{http://www.knubisoft.com/cott/testing/model/global-config}mobilebrowserAppiumServer"/&gt;
 *           &lt;element name="browserStack" type="{http://www.knubisoft.com/cott/testing/model/global-config}mobilebrowserBrowserStack"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mobilebrowser", propOrder = {
    "baseUrl",
    "appiumServer",
    "browserStack"
})
public class Mobilebrowser
    extends Settings
{

    @XmlElement(required = true)
    protected String baseUrl;
    protected MobilebrowserAppiumServer appiumServer;
    protected MobilebrowserBrowserStack browserStack;
    @XmlAttribute(name = "enabled", required = true)
    protected boolean enabled;

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
     * Gets the value of the appiumServer property.
     * 
     * @return
     *     possible object is
     *     {@link MobilebrowserAppiumServer }
     *     
     */
    public MobilebrowserAppiumServer getAppiumServer() {
        return appiumServer;
    }

    /**
     * Sets the value of the appiumServer property.
     * 
     * @param value
     *     allowed object is
     *     {@link MobilebrowserAppiumServer }
     *     
     */
    public void setAppiumServer(MobilebrowserAppiumServer value) {
        this.appiumServer = value;
    }

    /**
     * Gets the value of the browserStack property.
     * 
     * @return
     *     possible object is
     *     {@link MobilebrowserBrowserStack }
     *     
     */
    public MobilebrowserBrowserStack getBrowserStack() {
        return browserStack;
    }

    /**
     * Sets the value of the browserStack property.
     * 
     * @param value
     *     allowed object is
     *     {@link MobilebrowserBrowserStack }
     *     
     */
    public void setBrowserStack(MobilebrowserBrowserStack value) {
        this.browserStack = value;
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

}
