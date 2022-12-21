
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for settings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="settings"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="takeScreenshots" type="{http://www.knubisoft.com/cott/testing/model/global-config}takeScreenshot"/&gt;
 *         &lt;element name="elementAutowait" type="{http://www.knubisoft.com/cott/testing/model/global-config}elementAutowait"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "settings", propOrder = {
    "takeScreenshots",
    "elementAutowait"
})
@XmlSeeAlso({
    BrowserSettings.class,
    MobilebrowserDeviceSettings.class,
    Native.class
})
public class Settings {

    @XmlElement(required = true)
    protected TakeScreenshot takeScreenshots;
    @XmlElement(required = true)
    protected ElementAutowait elementAutowait;

    /**
     * Gets the value of the takeScreenshots property.
     * 
     * @return
     *     possible object is
     *     {@link TakeScreenshot }
     *     
     */
    public TakeScreenshot getTakeScreenshots() {
        return takeScreenshots;
    }

    /**
     * Sets the value of the takeScreenshots property.
     * 
     * @param value
     *     allowed object is
     *     {@link TakeScreenshot }
     *     
     */
    public void setTakeScreenshots(TakeScreenshot value) {
        this.takeScreenshots = value;
    }

    /**
     * Gets the value of the elementAutowait property.
     * 
     * @return
     *     possible object is
     *     {@link ElementAutowait }
     *     
     */
    public ElementAutowait getElementAutowait() {
        return elementAutowait;
    }

    /**
     * Sets the value of the elementAutowait property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElementAutowait }
     *     
     */
    public void setElementAutowait(ElementAutowait value) {
        this.elementAutowait = value;
    }

}
