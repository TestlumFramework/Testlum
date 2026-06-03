package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>Java class for ngrok complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ngrok"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="authToken" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="reservedDomain" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString" minOccurs="0"/&gt;
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
@XmlType(name = "ngrok", propOrder = {
        "authToken",
        "reservedDomain"
})
public class Ngrok {

    @XmlAttribute(name = "enabled", required = true)
    protected boolean enabled;
    @XmlElement(required = true)
    protected String authToken;
    protected String reservedDomain;

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
     * Gets the value of the authToken property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Sets the value of the authToken property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAuthToken(String value) {
        this.authToken = value;
    }

    /**
     * Gets the value of the reservedDomain property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getReservedDomain() {
        return reservedDomain;
    }

    /**
     * Sets the value of the reservedDomain property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setReservedDomain(String value) {
        this.reservedDomain = value;
    }
}
