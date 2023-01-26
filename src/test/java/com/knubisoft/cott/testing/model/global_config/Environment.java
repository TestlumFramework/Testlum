
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for environment complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="environment"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="uiConfigurations" type="{http://www.knubisoft.com/cott/testing/model/global-config}configFiles" minOccurs="0"/&gt;
 *         &lt;element name="integrationsConfigurations" type="{http://www.knubisoft.com/cott/testing/model/global-config}configFiles" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="folder" use="required" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString" /&gt;
 *       &lt;attribute name="enable" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "environment", propOrder = {
    "uiConfigurations",
    "integrationsConfigurations"
})
public class Environment {

    protected ConfigFiles uiConfigurations;
    protected ConfigFiles integrationsConfigurations;
    @XmlAttribute(name = "folder", required = true)
    protected String folder;
    @XmlAttribute(name = "enable", required = true)
    protected boolean enable;

    /**
     * Gets the value of the uiConfigurations property.
     * 
     * @return
     *     possible object is
     *     {@link ConfigFiles }
     *     
     */
    public ConfigFiles getUiConfigurations() {
        return uiConfigurations;
    }

    /**
     * Sets the value of the uiConfigurations property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConfigFiles }
     *     
     */
    public void setUiConfigurations(ConfigFiles value) {
        this.uiConfigurations = value;
    }

    /**
     * Gets the value of the integrationsConfigurations property.
     * 
     * @return
     *     possible object is
     *     {@link ConfigFiles }
     *     
     */
    public ConfigFiles getIntegrationsConfigurations() {
        return integrationsConfigurations;
    }

    /**
     * Sets the value of the integrationsConfigurations property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConfigFiles }
     *     
     */
    public void setIntegrationsConfigurations(ConfigFiles value) {
        this.integrationsConfigurations = value;
    }

    /**
     * Gets the value of the folder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Sets the value of the folder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFolder(String value) {
        this.folder = value;
    }

    /**
     * Gets the value of the enable property.
     * 
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * Sets the value of the enable property.
     * 
     */
    public void setEnable(boolean value) {
        this.enable = value;
    }

}
