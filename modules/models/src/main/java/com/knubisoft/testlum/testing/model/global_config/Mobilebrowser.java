
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for mobilebrowser complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mobilebrowser"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}settings"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="baseUrl" type="{http://www.knubisoft.com/testlum/testing/model/global-config}url"/&gt;
 *         &lt;element name="connection" type="{http://www.knubisoft.com/testlum/testing/model/global-config}connectionType"/&gt;
 *         &lt;element name="devices" type="{http://www.knubisoft.com/testlum/testing/model/global-config}mobilebrowserDevices"/&gt;
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
    "connection",
    "devices"
})
public class Mobilebrowser
    extends Settings
{

    @XmlElement(required = true)
    protected String baseUrl;
    @XmlElement(required = true)
    protected ConnectionType connection;
    @XmlElement(required = true)
    protected MobilebrowserDevices devices;
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
     * Gets the value of the connection property.
     * 
     * @return
     *     possible object is
     *     {@link ConnectionType }
     *     
     */
    public ConnectionType getConnection() {
        return connection;
    }

    /**
     * Sets the value of the connection property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConnectionType }
     *     
     */
    public void setConnection(ConnectionType value) {
        this.connection = value;
    }

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
