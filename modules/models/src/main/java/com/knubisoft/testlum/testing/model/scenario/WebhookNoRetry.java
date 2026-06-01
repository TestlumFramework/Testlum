package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>Java class for webhookNoRetry complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="webhookNoRetry"&gt;
 *   &lt;attribute name="timeout" type="{http://www.w3.org/2001/XMLSchema}int" use="required"/&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webhookNoRetry")
public class WebhookNoRetry {

    @XmlAttribute(name = "timeout", required = true)
    protected Integer timeout;

    /**
     * Gets the value of the timeout property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Sets the value of the timeout property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setTimeout(Integer value) {
        this.timeout = value;
    }

}
