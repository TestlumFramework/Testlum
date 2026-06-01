package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>Java class for webhookResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="webhookResponse"&gt;
 *   &lt;sequence&gt;
 *     &lt;element name="expected" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookExpected" minOccurs="0"/&gt;
 *     &lt;element name="mockReturn" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookMockReturn" minOccurs="0"/&gt;
 *   &lt;/sequence&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webhookResponse", propOrder = {
        "expected",
        "mockReturn"
})
public class WebhookResponse {

    protected WebhookExpected expected;
    protected WebhookMockReturn mockReturn;

    /**
     * Gets the value of the expected property.
     *
     * @return possible object is
     * {@link WebhookExpected }
     *
     */
    public WebhookExpected getExpected() {
        return expected;
    }

    /**
     * Sets the value of the expected property.
     *
     * @param expected allowed object is
     *                   {@link WebhookExpected }
     *
     */
    public void setExpected(WebhookExpected expected) {
        this.expected = expected;
    }

    /**
     * Gets the value of the mockReturn property.
     *
     * @return possible object is
     * {@link WebhookMockReturn }
     *
     */
    public WebhookMockReturn getMockReturn() {
        return mockReturn;
    }

    /**
     * Sets the value of the mockReturn property.
     *
     * @param mockReturn allowed object is
     *                   {@link WebhookMockReturn }
     *
     */
    public void setMockReturn(WebhookMockReturn mockReturn) {
        this.mockReturn = mockReturn;
    }

}