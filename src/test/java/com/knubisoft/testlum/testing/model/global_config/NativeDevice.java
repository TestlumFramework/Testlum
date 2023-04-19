
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nativeDevice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nativeDevice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}abstractDevice"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="appiumCapabilities" type="{http://www.knubisoft.com/testlum/testing/model/global-config}appiumNativeCapabilities"/&gt;
 *         &lt;element name="browserStackCapabilities" type="{http://www.knubisoft.com/testlum/testing/model/global-config}browserStackNativeCapabilities"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nativeDevice", propOrder = {
    "appiumCapabilities",
    "browserStackCapabilities"
})
public class NativeDevice
    extends AbstractDevice
{

    protected AppiumNativeCapabilities appiumCapabilities;
    protected BrowserStackNativeCapabilities browserStackCapabilities;

    /**
     * Gets the value of the appiumCapabilities property.
     * 
     * @return
     *     possible object is
     *     {@link AppiumNativeCapabilities }
     *     
     */
    public AppiumNativeCapabilities getAppiumCapabilities() {
        return appiumCapabilities;
    }

    /**
     * Sets the value of the appiumCapabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link AppiumNativeCapabilities }
     *     
     */
    public void setAppiumCapabilities(AppiumNativeCapabilities value) {
        this.appiumCapabilities = value;
    }

    /**
     * Gets the value of the browserStackCapabilities property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserStackNativeCapabilities }
     *     
     */
    public BrowserStackNativeCapabilities getBrowserStackCapabilities() {
        return browserStackCapabilities;
    }

    /**
     * Sets the value of the browserStackCapabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserStackNativeCapabilities }
     *     
     */
    public void setBrowserStackCapabilities(BrowserStackNativeCapabilities value) {
        this.browserStackCapabilities = value;
    }

}
