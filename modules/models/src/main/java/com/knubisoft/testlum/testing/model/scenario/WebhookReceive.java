package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * <p>Java class for webhookReceive complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="webhookReceive"&gt;
 *   &lt;attribute name="expected" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" use="required"/&gt;
 *   &lt;attribute name="forward" type="{http://www.w3.org/2001/XMLSchema}boolean" use="required" /&gt;
 *   &lt;attribute name="alias" type="{http://www.knubisoft.com/testlum/testing/model/scenario}aliasPattern" /&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WebhookReceive {

    @XmlAttribute(name = "expected", required = true)
    protected String expected;
    @XmlAttribute(name = "forward", required = true)
    protected Boolean forward;
    @XmlAttribute(name = "alias")
    protected String alias;

    /**
     * Gets the value of the expected property.
     *
     * @return possible object is
     * {@link String }
     *
     */
    public String getExpected() {
        return expected;
    }

    /**
     * Sets the value of the expected property.
     *
     * @param value allowed object is
     *              {@link String }
     *
     */
    public void setExpected(String value) {
        this.expected = value;
    }

    /**
     * Gets the value of the forward property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isForward() {
        return forward;
    }

    /**
     * Sets the value of the forward property.
     *
     * @param forward
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setForward(Boolean forward) {
        this.forward = forward;
    }

    /**
     * Gets the value of the alias property.
     *
     * @return possible object is
     * {@link String }
     *
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     *
     * @param value allowed object is
     *              {@link String }
     *
     */
    public void setAlias(String value) {
        this.alias = value;
    }

}
