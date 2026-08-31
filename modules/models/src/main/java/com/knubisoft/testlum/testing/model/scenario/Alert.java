
package com.knubisoft.testlum.testing.model.scenario;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for alert complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="alert"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;attribute name="type" type="{http://www.knubisoft.com/testlum/testing/model/scenario}alertType" default="alert" /&gt;
 *       &lt;attribute name="action" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}alertAction" /&gt;
 *       &lt;attribute name="text" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="waitUntilVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="timeout" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" default="10" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "alert")
public class Alert
    extends AbstractUiCommand
{

    @XmlAttribute(name = "type")
    protected AlertType type;
    @XmlAttribute(name = "action", required = true)
    protected AlertAction action;
    @XmlAttribute(name = "text")
    protected String text;
    @XmlAttribute(name = "waitUntilVisible")
    protected Boolean waitUntilVisible;
    @XmlAttribute(name = "timeout")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger timeout;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link AlertType }
     *     
     */
    public AlertType getType() {
        if (type == null) {
            return AlertType.ALERT;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link AlertType }
     *     
     */
    public void setType(AlertType value) {
        this.type = value;
    }

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link AlertAction }
     *     
     */
    public AlertAction getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link AlertAction }
     *     
     */
    public void setAction(AlertAction value) {
        this.action = value;
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Gets the value of the waitUntilVisible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isWaitUntilVisible() {
        return waitUntilVisible;
    }

    /**
     * Sets the value of the waitUntilVisible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWaitUntilVisible(Boolean value) {
        this.waitUntilVisible = value;
    }

    /**
     * Gets the value of the timeout property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTimeout() {
        if (timeout == null) {
            return new BigInteger("10");
        } else {
            return timeout;
        }
    }

    /**
     * Sets the value of the timeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTimeout(BigInteger value) {
        this.timeout = value;
    }

}
