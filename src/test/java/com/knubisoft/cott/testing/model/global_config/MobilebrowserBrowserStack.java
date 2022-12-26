
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mobilebrowserBrowserStack complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mobilebrowserBrowserStack"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="devices" type="{http://www.knubisoft.com/cott/testing/model/global-config}mobilebrowserDevices"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mobilebrowserBrowserStack", propOrder = {
    "devices"
})
public class MobilebrowserBrowserStack {

    @XmlElement(required = true)
    protected MobilebrowserDevices devices;

    /**
     * Gets the value of the devices property.
     * 
     * @return
     *     possible object is
     *     {@link MobilebrowserDevices }
     *     
     */
    public MobilebrowserDevices getDevices() {
        return devices;
    }

    /**
     * Sets the value of the devices property.
     * 
     * @param value
     *     allowed object is
     *     {@link MobilebrowserDevices }
     *     
     */
    public void setDevices(MobilebrowserDevices value) {
        this.devices = value;
    }

}
