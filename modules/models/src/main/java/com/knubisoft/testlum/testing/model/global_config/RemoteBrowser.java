
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remoteBrowser complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteBrowser"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="browserVersion" use="required" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString" /&gt;
 *       &lt;attribute name="remoteBrowserURL" use="required" type="{http://www.knubisoft.com/testlum/testing/model/global-config}url" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteBrowser")
public class RemoteBrowser {

    @XmlAttribute(name = "browserVersion", required = true)
    protected String browserVersion;
    @XmlAttribute(name = "remoteBrowserURL", required = true)
    protected String remoteBrowserURL;

    /**
     * Gets the value of the browserVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBrowserVersion() {
        return browserVersion;
    }

    /**
     * Sets the value of the browserVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBrowserVersion(String value) {
        this.browserVersion = value;
    }

    /**
     * Gets the value of the remoteBrowserURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemoteBrowserURL() {
        return remoteBrowserURL;
    }

    /**
     * Sets the value of the remoteBrowserURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemoteBrowserURL(String value) {
        this.remoteBrowserURL = value;
    }

}
