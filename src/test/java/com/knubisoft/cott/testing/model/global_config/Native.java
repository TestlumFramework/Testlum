
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for native complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="native"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/global-config}settings"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="appiumServer" type="{http://www.knubisoft.com/cott/testing/model/global-config}nativeAppiumServer"/&gt;
 *         &lt;element name="browserStack" type="{http://www.knubisoft.com/cott/testing/model/global-config}nativeBrowserStack"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "native", propOrder = {
    "appiumServer",
    "browserStack"
})
public class Native
    extends Settings
{

    protected NativeAppiumServer appiumServer;
    protected NativeBrowserStack browserStack;
    @XmlAttribute(name = "enabled", required = true)
    protected boolean enabled;

    /**
     * Gets the value of the appiumServer property.
     * 
     * @return
     *     possible object is
     *     {@link NativeAppiumServer }
     *     
     */
    public NativeAppiumServer getAppiumServer() {
        return appiumServer;
    }

    /**
     * Sets the value of the appiumServer property.
     * 
     * @param value
     *     allowed object is
     *     {@link NativeAppiumServer }
     *     
     */
    public void setAppiumServer(NativeAppiumServer value) {
        this.appiumServer = value;
    }

    /**
     * Gets the value of the browserStack property.
     * 
     * @return
     *     possible object is
     *     {@link NativeBrowserStack }
     *     
     */
    public NativeBrowserStack getBrowserStack() {
        return browserStack;
    }

    /**
     * Sets the value of the browserStack property.
     * 
     * @param value
     *     allowed object is
     *     {@link NativeBrowserStack }
     *     
     */
    public void setBrowserStack(NativeBrowserStack value) {
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
