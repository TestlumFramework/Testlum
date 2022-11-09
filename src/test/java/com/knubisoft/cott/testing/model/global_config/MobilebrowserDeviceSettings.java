
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mobilebrowserDeviceSettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mobilebrowserDeviceSettings"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/global-config}settings"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="devices" type="{http://www.knubisoft.com/cott/testing/model/global-config}mobilebrowserDevices"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mobilebrowserDeviceSettings", propOrder = {
    "devices"
})
public class MobilebrowserDeviceSettings
    extends Settings
{

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
