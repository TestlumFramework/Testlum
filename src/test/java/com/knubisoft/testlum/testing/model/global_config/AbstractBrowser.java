
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for abstractBrowser complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="abstractBrowser"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="browserType" type="{http://www.knubisoft.com/testlum/testing/model/global-config}browserType"/&gt;
 *         &lt;element name="capabilities" type="{http://www.knubisoft.com/testlum/testing/model/global-config}capabilities" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="maximizedBrowserWindow" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="browserWindowSize" type="{http://www.knubisoft.com/testlum/testing/model/global-config}windowSize" /&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/global-config}aliasPattern" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "abstractBrowser", propOrder = {
    "browserType",
    "capabilities"
})
@XmlSeeAlso({
    Chrome.class,
    Firefox.class,
    Edge.class,
    Opera.class,
    Safari.class
})
public abstract class AbstractBrowser {

    @XmlElement(required = true)
    protected BrowserType browserType;
    protected Capabilities capabilities;
    @XmlAttribute(name = "enabled", required = true)
    protected boolean enabled;
    @XmlAttribute(name = "maximizedBrowserWindow", required = true)
    protected boolean maximizedBrowserWindow;
    @XmlAttribute(name = "browserWindowSize")
    protected String browserWindowSize;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the browserType property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserType }
     *     
     */
    public BrowserType getBrowserType() {
        return browserType;
    }

    /**
     * Sets the value of the browserType property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserType }
     *     
     */
    public void setBrowserType(BrowserType value) {
        this.browserType = value;
    }

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
     * Gets the value of the maximizedBrowserWindow property.
     * 
     */
    public boolean isMaximizedBrowserWindow() {
        return maximizedBrowserWindow;
    }

    /**
     * Sets the value of the maximizedBrowserWindow property.
     * 
     */
    public void setMaximizedBrowserWindow(boolean value) {
        this.maximizedBrowserWindow = value;
    }

    /**
     * Gets the value of the browserWindowSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBrowserWindowSize() {
        return browserWindowSize;
    }

    /**
     * Sets the value of the browserWindowSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBrowserWindowSize(String value) {
        this.browserWindowSize = value;
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
