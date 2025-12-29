
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

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
 *       &lt;attribute name="until"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="visible"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="timeout" type="{http://www.w3.org/2001/XMLSchema}int" default="10" /&gt;
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
    @XmlAttribute(name = "until")
    protected String until;
    @XmlAttribute(name = "timeout")
    protected Integer timeout;

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
     * Gets the value of the until property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUntil() {
        return until;
    }

    /**
     * Sets the value of the until property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUntil(String value) {
        this.until = value;
    }

    /**
     * Gets the value of the timeout property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getTimeout() {
        if (timeout == null) {
            return  10;
        } else {
            return timeout;
        }
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
