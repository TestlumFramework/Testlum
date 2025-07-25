
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mobilebrowserDevice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mobilebrowserDevice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}abstractDevice"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="appiumCapabilities" type="{http://www.knubisoft.com/testlum/testing/model/global-config}appiumCapabilities"/&gt;
 *         &lt;element name="browserStackCapabilities" type="{http://www.knubisoft.com/testlum/testing/model/global-config}browserStackCapabilities"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mobilebrowserDevice", propOrder = {
    "appiumCapabilities",
    "browserStackCapabilities"
})
public class MobilebrowserDevice
    extends AbstractDevice
{

    protected AppiumCapabilities appiumCapabilities;
    protected BrowserStackCapabilities browserStackCapabilities;

    /**
     * Gets the value of the appiumCapabilities property.
     * 
     * @return
     *     possible object is
     *     {@link AppiumCapabilities }
     *     
     */
    public AppiumCapabilities getAppiumCapabilities() {
        return appiumCapabilities;
    }

    /**
     * Sets the value of the appiumCapabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link AppiumCapabilities }
     *     
     */
    public void setAppiumCapabilities(AppiumCapabilities value) {
        this.appiumCapabilities = value;
    }

    /**
     * Gets the value of the browserStackCapabilities property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserStackCapabilities }
     *     
     */
    public BrowserStackCapabilities getBrowserStackCapabilities() {
        return browserStackCapabilities;
    }

    /**
     * Sets the value of the browserStackCapabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserStackCapabilities }
     *     
     */
    public void setBrowserStackCapabilities(BrowserStackCapabilities value) {
        this.browserStackCapabilities = value;
    }

}
