
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for web complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="web"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="baseUrl" type="{http://www.knubisoft.com/testlum/testing/model/global-config}urlWeb"/&gt;
 *         &lt;element name="browserSettings" type="{http://www.knubisoft.com/testlum/testing/model/global-config}browserSettings"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "web", propOrder = {
    "baseUrl",
    "browserSettings"
})
public class Web {

    @XmlElement(required = true)
    protected String baseUrl;
    @XmlElement(required = true)
    protected BrowserSettings browserSettings;
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
     * Gets the value of the browserSettings property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserSettings }
     *     
     */
    public BrowserSettings getBrowserSettings() {
        return browserSettings;
    }

    /**
     * Sets the value of the browserSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserSettings }
     *     
     */
    public void setBrowserSettings(BrowserSettings value) {
        this.browserSettings = value;
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
