
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for abstractDevice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="abstractDevice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="capabilities" type="{http://www.knubisoft.com/testlum/testing/model/global-config}capabilities" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="platformName" use="required" type="{http://www.knubisoft.com/testlum/testing/model/global-config}platform" /&gt;
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/global-config}aliasPattern" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "abstractDevice", propOrder = {
    "capabilities"
})
@XmlSeeAlso({
    MobilebrowserDevice.class,
    NativeDevice.class
})
public abstract class AbstractDevice {

    protected Capabilities capabilities;
    @XmlAttribute(name = "platformName", required = true)
    protected Platform platformName;
    @XmlAttribute(name = "enabled", required = true)
    protected boolean enabled;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the capabilities property.
     * 
     * @return
     *     possible object is
     *     {@link Capabilities }
     *     
     */
    public Capabilities getCapabilities() {
        return capabilities;
    }

    /**
     * Sets the value of the capabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link Capabilities }
     *     
     */
    public void setCapabilities(Capabilities value) {
        this.capabilities = value;
    }

    /**
     * Gets the value of the platformName property.
     * 
     * @return
     *     possible object is
     *     {@link Platform }
     *     
     */
    public Platform getPlatformName() {
        return platformName;
    }

    /**
     * Sets the value of the platformName property.
     * 
     * @param value
     *     allowed object is
     *     {@link Platform }
     *     
     */
    public void setPlatformName(Platform value) {
        this.platformName = value;
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

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
    }

}
