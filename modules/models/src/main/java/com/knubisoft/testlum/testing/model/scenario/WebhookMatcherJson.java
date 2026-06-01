package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>Java class for webhookMatcherJson complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="webhookMatcherJson"&gt;
 *   &lt;sequence&gt;
 *     &lt;element name="jpath" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *     &lt;element name="value" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *   &lt;/sequence&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webhookMatcherJson", propOrder = {
        "jpath",
        "value"
})
public class WebhookMatcherJson {

    protected String jpath;
    protected String value;

    /**
     * Gets the value of the jpath property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getJpath() {
        return jpath;
    }

    /**
     * Sets the value of the jpath property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setJpath(String value) {
        this.jpath = value;
    }

    /**
     * Gets the value of the value property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setValue(String value) {
        this.value = value;
    }

}
