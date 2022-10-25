
package com.knubisoft.cott.testing.model.scenario;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for webSocketReceive complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="webSocketReceive"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="message" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="file" type="{http://www.knubisoft.com/cott/testing/model/scenario}expectedPattern"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="topic" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}endpointPattern" /&gt;
 *       &lt;attribute name="compareRule" type="{http://www.knubisoft.com/cott/testing/model/scenario}compareRule" default="equals" /&gt;
 *       &lt;attribute name="valuesNumber" type="{http://www.w3.org/2001/XMLSchema}integer" default="1" /&gt;
 *       &lt;attribute name="timeoutMillis" type="{http://www.w3.org/2001/XMLSchema}long" default="5000" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webSocketReceive", propOrder = {
    "message",
    "file"
})
public class WebSocketReceive {

    protected String message;
    protected String file;
    @XmlAttribute(name = "topic", required = true)
    protected String topic;
    @XmlAttribute(name = "compareRule")
    protected CompareRule compareRule;
    @XmlAttribute(name = "valuesNumber")
    protected BigInteger valuesNumber;
    @XmlAttribute(name = "timeoutMillis")
    protected Long timeoutMillis;

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
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
     * Gets the value of the topic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Sets the value of the topic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTopic(String value) {
        this.topic = value;
    }

    /**
     * Gets the value of the compareRule property.
     * 
     * @return
     *     possible object is
     *     {@link CompareRule }
     *     
     */
    public CompareRule getCompareRule() {
        if (compareRule == null) {
            return CompareRule.EQUALS;
        } else {
            return compareRule;
        }
    }

    /**
     * Sets the value of the compareRule property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompareRule }
     *     
     */
    public void setCompareRule(CompareRule value) {
        this.compareRule = value;
    }

    /**
     * Gets the value of the valuesNumber property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getValuesNumber() {
        if (valuesNumber == null) {
            return new BigInteger("1");
        } else {
            return valuesNumber;
        }
    }

    /**
     * Sets the value of the valuesNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setValuesNumber(BigInteger value) {
        this.valuesNumber = value;
    }

    /**
     * Gets the value of the timeoutMillis property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getTimeoutMillis() {
        if (timeoutMillis == null) {
            return  5000L;
        } else {
            return timeoutMillis;
        }
    }

    /**
     * Sets the value of the timeoutMillis property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTimeoutMillis(Long value) {
        this.timeoutMillis = value;
    }

}
