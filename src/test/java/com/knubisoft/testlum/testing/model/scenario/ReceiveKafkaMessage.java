
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for receiveKafkaMessage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="receiveKafkaMessage"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="value" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="file" type="{http://www.knubisoft.com/testlum/testing/model/scenario}expectedPattern"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="topic" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="timeoutMillis" type="{http://www.w3.org/2001/XMLSchema}long" default="1500" /&gt;
 *       &lt;attribute name="headers" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "receiveKafkaMessage", propOrder = {
    "value",
    "file"
})
public class ReceiveKafkaMessage {

    protected String value;
    protected String file;
    @XmlAttribute(name = "topic", required = true)
    protected String topic;
    @XmlAttribute(name = "timeoutMillis")
    protected Long timeoutMillis;
    @XmlAttribute(name = "headers")
    protected Boolean headers;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
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
     * Gets the value of the timeoutMillis property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getTimeoutMillis() {
        if (timeoutMillis == null) {
            return  1500L;
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

    /**
     * Gets the value of the headers property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHeaders() {
        if (headers == null) {
            return true;
        } else {
            return headers;
        }
    }

    /**
     * Sets the value of the headers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHeaders(Boolean value) {
        this.headers = value;
    }

}
