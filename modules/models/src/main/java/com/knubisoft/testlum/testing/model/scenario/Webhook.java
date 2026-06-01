
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
 *         &lt;element name="receive" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookReceive"/&gt;
 *         &lt;element name="response" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookResponse" minOccurs="0"/&gt;
 *         &lt;element name="retry" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookRetry" minOccurs="0"/&gt;
 *         &lt;element name="noRetry" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookNoRetry" minOccurs="0"/&gt;
 *         &lt;element name="webhookMatcher" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookMatcher" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="timeout" type="{http://www.w3.org/2001/XMLSchema}int" use="required"/&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webhook", propOrder = {
    "receive",
    "response",
    "retry",
    "noRetry",
    "webhookMatcher"
})
public class Webhook
    extends AbstractCommand
{

    protected WebhookReceive receive;
    protected WebhookResponse response;
    protected WebhookRetry retry;
    protected WebhookNoRetry noRetry;
    protected WebhookMatcher webhookMatcher;
    @XmlAttribute(name = "timeout", required = true)
    protected Integer timeout;

    /**
     * Gets the value of the receive property.
     * 
     * @return
     *     possible object is
     *     {@link WebhookReceive }
     *     
     */
    public WebhookReceive getReceive() {
        return receive;
    }

    /**
     * Sets the value of the receive property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebhookReceive }
     *     
     */
    public void setReceive(WebhookReceive value) {
        this.receive = value;
    }

    /**
     * Gets the value of the response property.
     * 
     * @return
     *     possible object is
     *     {@link WebhookResponse }
     *     
     */
    public WebhookResponse getResponse() {
        return response;
    }

    /**
     * Sets the value of the response property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebhookResponse }
     *     
     */
    public void setResponse(WebhookResponse value) {
        this.response = value;
    }

    /**
     * Gets the value of the retry property.
     * 
     * @return
     *     possible object is
     *     {@link WebhookRetry }
     *     
     */
    public WebhookRetry getRetry() {
        return retry;
    }

    /**
     * Sets the value of the retry property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebhookRetry }
     *     
     */
    public void setRetry(WebhookRetry value) {
        this.retry = value;
    }

    /**
     * Gets the value of the noRetry property.
     * 
     * @return
     *     possible object is
     *     {@link WebhookNoRetry }
     *     
     */
    public WebhookNoRetry getNoRetry() {
        return noRetry;
    }

    /**
     * Sets the value of the noRetry property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebhookNoRetry }
     *     
     */
    public void setNoRetry(WebhookNoRetry value) {
        this.noRetry = value;
    }

    /**
     * Gets the value of the webhookMatcher property.
     * 
     * @return
     *     possible object is
     *     {@link ElasticsearchHeadRequest }
     *     
     */
    public WebhookMatcher getWebhookMatcher() {
        return webhookMatcher;
    }

    /**
     * Sets the value of the webhookMatcher property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElasticsearchHeadRequest }
     *     
     */
    public void setWebhookMatcher(WebhookMatcher value) {
        this.webhookMatcher = value;
    }

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
