
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for browserInDocker complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="browserInDocker"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="screenRecording" type="{http://www.knubisoft.com/testlum/testing/model/global-config}screenRecording" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="browserVersion" use="required" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString" /&gt;
 *       &lt;attribute name="enableVNC" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="dockerNetwork" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "browserInDocker", propOrder = {
    "screenRecording"
})
public class BrowserInDocker {

    protected ScreenRecording screenRecording;
    @XmlAttribute(name = "browserVersion", required = true)
    protected String browserVersion;
    @XmlAttribute(name = "enableVNC", required = true)
    protected boolean enableVNC;
    @XmlAttribute(name = "dockerNetwork")
    protected String dockerNetwork;

    /**
     * Gets the value of the screenRecording property.
     * 
     * @return
     *     possible object is
     *     {@link ScreenRecording }
     *     
     */
    public ScreenRecording getScreenRecording() {
        return screenRecording;
    }

    /**
     * Sets the value of the screenRecording property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScreenRecording }
     *     
     */
    public void setScreenRecording(ScreenRecording value) {
        this.screenRecording = value;
    }

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
     * Gets the value of the enableVNC property.
     * 
     */
    public boolean isEnableVNC() {
        return enableVNC;
    }

    /**
     * Sets the value of the enableVNC property.
     * 
     */
    public void setEnableVNC(boolean value) {
        this.enableVNC = value;
    }

    /**
     * Gets the value of the dockerNetwork property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDockerNetwork() {
        return dockerNetwork;
    }

    /**
     * Sets the value of the dockerNetwork property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDockerNetwork(String value) {
        this.dockerNetwork = value;
    }

}
