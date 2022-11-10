
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for webSocketSendReceive complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="webSocketSendReceive"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="message" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString"/&gt;
 *           &lt;element name="file" type="{http://www.knubisoft.com/cott/testing/model/scenario}bodyFile"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice&gt;
 *           &lt;element name="expected" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="endpoint" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}endpointPattern" /&gt;
 *       &lt;attribute name="topic" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}endpointPattern" /&gt;
 *       &lt;attribute name="valuesNumber" type="{http://www.w3.org/2001/XMLSchema}int" default="0" /&gt;
 *       &lt;attribute name="timeoutMillis" type="{http://www.w3.org/2001/XMLSchema}int" default="5000" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webSocketSendReceive", propOrder = {
    "message",
    "file",
    "expected"
})
public class WebSocketSendReceive {

    protected String message;
    protected String file;
    protected String expected;
    @XmlAttribute(name = "endpoint", required = true)
    protected String endpoint;
    @XmlAttribute(name = "topic", required = true)
    protected String topic;
    @XmlAttribute(name = "valuesNumber")
    protected Integer valuesNumber;
    @XmlAttribute(name = "timeoutMillis")
    protected Integer timeoutMillis;

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
     * Gets the value of the endpoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the value of the endpoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndpoint(String value) {
        this.endpoint = value;
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
     * Gets the value of the valuesNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getValuesNumber() {
        if (valuesNumber == null) {
            return  0;
        } else {
            return valuesNumber;
        }
    }

    /**
     * Sets the value of the valuesNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setValuesNumber(Integer value) {
        this.valuesNumber = value;
    }

    /**
     * Gets the value of the timeoutMillis property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getTimeoutMillis() {
        if (timeoutMillis == null) {
            return  5000;
        } else {
            return timeoutMillis;
        }
    }

    /**
     * Sets the value of the timeoutMillis property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTimeoutMillis(Integer value) {
        this.timeoutMillis = value;
    }

}
