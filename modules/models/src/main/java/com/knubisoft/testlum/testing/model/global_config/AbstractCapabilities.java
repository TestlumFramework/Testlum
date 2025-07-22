
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for abstractCapabilities complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="abstractCapabilities"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="deviceName" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="platformVersion" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "abstractCapabilities", propOrder = {
    "deviceName",
    "platformVersion"
})
@XmlSeeAlso({
    AppiumCapabilities.class,
    BrowserStackCapabilities.class
})
public abstract class AbstractCapabilities {

    @XmlElement(required = true)
    protected String deviceName;
    @XmlElement(required = true)
    protected String platformVersion;

    /**
     * Gets the value of the deviceName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Sets the value of the deviceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceName(String value) {
        this.deviceName = value;
    }

    /**
     * Gets the value of the platformVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlatformVersion() {
        return platformVersion;
    }

    /**
     * Sets the value of the platformVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlatformVersion(String value) {
        this.platformVersion = value;
    }

}
