
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for webhook complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="webhook"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="status" type="{http://www.knubisoft.com/testlum/testing/model/scenario}codePattern" minOccurs="0"/&gt;
 *         &lt;element name="expected" type="{http://www.knubisoft.com/testlum/testing/model/scenario}expectedPattern" minOccurs="0"/&gt;
 *         &lt;element name="headers" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookHeaders" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}aliasPattern" /&gt;
 *       &lt;attribute name="timeout" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}positiveIntegerMin1" /&gt;
 *       &lt;attribute name="matchValue" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webhook", propOrder = {
    "status",
    "expected",
    "headers"
})
public class Webhook
    extends AbstractCommand
{

    protected Integer status;
    protected String expected;
    protected WebhookHeaders headers;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;
    @XmlAttribute(name = "timeout", required = true)
    protected Integer timeout;
    @XmlAttribute(name = "matchValue")
    protected String matchValue;

    /**
     * Gets the value of the status property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setStatus(Integer value) {
        this.status = value;
    }

    /**
     * Gets the value of the expected property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getExpected() {
        return expected;
    }

    /**
     * Sets the value of the expected property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setExpected(String value) {
        this.expected = value;
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

    /**
     * Gets the value of the timeout property (seconds).
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

    /**
     * Gets the value of the matchValue property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMatchValue() {
        return matchValue;
    }

    /**
     * Sets the value of the matchValue property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMatchValue(String value) {
        this.matchValue = value;
    }

}
