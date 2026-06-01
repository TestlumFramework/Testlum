package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>Java class for webhookExpected complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="webhookExpected"&gt;
 *   &lt;choice maxOccurs="unbounded"&gt;
 *     &lt;element name="status" type="{http://www.knubisoft.com/testlum/testing/model/scenario}codePattern"/&gt;
 *     &lt;element name="file" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *     &lt;element name="headers" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookHeaders"/&gt;
 *   &lt;/choice&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webhookExpected", propOrder = {
        "code",
        "file",
        "headers"
})
public class WebhookExpected {

    protected Integer code;
    protected String file;
    protected WebhookHeaders headers;

    /**
     * Gets the value of the code property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setCode(Integer value) {
        this.code = value;
    }

    /**
     * Gets the value of the file property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFile(String value) {
        this.file = value;
    }

    /**
     * Gets the value of the headers property.
     *
     * @return
     *     possible object is
     *     {@link WebhookHeaders }
     *
     */
    public WebhookHeaders getHeaders() {
        return headers;
    }

    /**
     * Sets the value of the headers property.
     *
     * @param value
     *     allowed object is
     *     {@link WebhookHeaders }
     *
     */
    public void setHeaders(WebhookHeaders value) {
        this.headers = value;
    }

}
