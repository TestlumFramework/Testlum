package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Java class for webhookRetry complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="webhookRetry"&gt;
 *   &lt;choice&gt;
 *     &lt;sequence&gt;
 *       &lt;element name="receive" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookReceive"/&gt;
 *       &lt;element name="response" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookResponse" minOccurs="0"/&gt;
 *     &lt;/sequence&gt;
 *     &lt;element name="attempt" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookRetryAttempt" maxOccurs="unbounded"/&gt;
 *   &lt;/choice&gt;
 *   &lt;attribute name="min" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *   &lt;attribute name="max" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *   &lt;attribute name="idleTimeout" type="{http://www.w3.org/2001/XMLSchema}int" use="required"/&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webhookRetry", propOrder = {
        "receive",
        "response",
        "attempt"
})
public class WebhookRetry {

    protected WebhookReceive receive;
    protected WebhookResponse response;
    protected List<WebhookRetryAttempt> attempt;
    @XmlAttribute(name = "min")
    protected Integer min;
    @XmlAttribute(name = "max")
    protected Integer max;
    @XmlAttribute(name = "idleTimeout", required = true)
    protected Integer idleTimeout;

    /**
     * Gets the value of the receive property.
     *
     * @return possible object is
     * {@link WebhookReceive }
     *
     */
    public WebhookReceive getReceive() {
        return receive;
    }

    /**
     * Sets the value of the receive property.
     *
     * @param value allowed object is
     *              {@link WebhookReceive }
     *
     */
    public void setReceive(WebhookReceive value) {
        this.receive = value;
    }

    /**
     * Gets the value of the response property.
     *
     * @return possible object is
     * {@link WebhookResponse }
     *
     */
    public WebhookResponse getResponse() {
        return response;
    }

    /**
     * Sets the value of the response property.
     *
     * @param value allowed object is
     *              {@link WebhookResponse }
     *
     */
    public void setResponse(WebhookResponse value) {
        this.response = value;
    }

    /**
     * Gets the value of the attempt property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the param property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttempt().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WebhookRetryAttempt }
     *
     *
     */
    public List<WebhookRetryAttempt> getAttempt() {
        if (attempt == null) {
            attempt = new ArrayList<WebhookRetryAttempt>();
        }
        return this.attempt;
    }

    /**
     * Gets the value of the min property.
     *
     * @return possible object is
     * {@link Integer }
     *
     */
    public Integer getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     *
     * @param value allowed object is
     *              {@link Integer }
     *
     */
    public void setMin(Integer value) {
        this.min = value;
    }

    /**
     * Gets the value of the max property.
     *
     * @return possible object is
     * {@link Integer }
     *
     */
    public Integer getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     *
     * @param value allowed object is
     *              {@link Integer }
     *
     */
    public void setMax(Integer value) {
        this.max = value;
    }

    /**
     * Gets the value of the idleTimeout property.
     *
     * @return possible object is
     * {@link Integer }
     *
     */
    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Sets the value of the idleTimeout property.
     *
     * @param value allowed object is
     *              {@link Integer }
     *
     */
    public void setIdleTimeout(Integer value) {
        this.idleTimeout = value;
    }

}
