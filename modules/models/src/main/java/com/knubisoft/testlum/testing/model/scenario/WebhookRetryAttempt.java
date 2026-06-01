package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>Java class for webhookRetryAttempt complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="webhookRetryAttempt"&gt;
 *   &lt;sequence&gt;
 *     &lt;element name="receive" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookReceive"/&gt;
 *     &lt;element name="response" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookResponse" minOccurs="0"/&gt;
 *   &lt;/sequence&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webhookRetryAttempt", propOrder = {
        "receive",
        "response"
})
public class WebhookRetryAttempt {

    protected WebhookReceive receive;
    protected WebhookResponse response;

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

}
