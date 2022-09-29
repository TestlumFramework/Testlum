
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nativeDevice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nativeDevice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/global-config}abstractDevice"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="appPackage" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="appActivity" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nativeDevice", propOrder = {
    "appPackage",
    "appActivity"
})
public class NativeDevice
    extends AbstractDevice
{

    @XmlElement(required = true)
    protected String appPackage;
    @XmlElement(required = true)
    protected String appActivity;

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

}
