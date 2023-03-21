
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for desktopType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="desktopType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="appiumCapabilities" type="{http://www.knubisoft.com/cott/testing/model/global-config}appiumDesktopCapabilities"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="platformName" use="required" type="{http://www.knubisoft.com/cott/testing/model/global-config}desktopPlatform" /&gt;
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/cott/testing/model/global-config}aliasPattern" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "desktopType", propOrder = {
    "appiumCapabilities"
})
public class DesktopType {

    protected AppiumDesktopCapabilities appiumCapabilities;
    @XmlAttribute(name = "platformName", required = true)
    protected DesktopPlatform platformName;
    @XmlAttribute(name = "enabled", required = true)
    protected boolean enabled;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the appiumCapabilities property.
     * 
     * @return
     *     possible object is
     *     {@link AppiumDesktopCapabilities }
     *     
     */
    public AppiumDesktopCapabilities getAppiumCapabilities() {
        return appiumCapabilities;
    }

    /**
     * Sets the value of the appiumCapabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link AppiumDesktopCapabilities }
     *     
     */
    public void setAppiumCapabilities(AppiumDesktopCapabilities value) {
        this.appiumCapabilities = value;
    }

    /**
     * Gets the value of the platformName property.
     * 
     * @return
     *     possible object is
     *     {@link DesktopPlatform }
     *     
     */
    public DesktopPlatform getPlatformName() {
        return platformName;
    }

    /**
     * Sets the value of the platformName property.
     * 
     * @param value
     *     allowed object is
     *     {@link DesktopPlatform }
     *     
     */
    public void setPlatformName(DesktopPlatform value) {
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
