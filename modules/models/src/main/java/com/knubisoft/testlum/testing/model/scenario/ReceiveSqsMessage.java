
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for receiveSqsMessage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="receiveSqsMessage"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="value" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="file" type="{http://www.knubisoft.com/testlum/testing/model/scenario}expectedPattern"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="queue" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="maxNumberOfMessages" type="{http://www.knubisoft.com/testlum/testing/model/scenario}positiveIntegerMin1" /&gt;
 *       &lt;attribute name="visibilityTimeout" type="{http://www.knubisoft.com/testlum/testing/model/scenario}positiveIntegerMin1" /&gt;
 *       &lt;attribute name="waitTimeSeconds" type="{http://www.knubisoft.com/testlum/testing/model/scenario}positiveIntegerMin1" /&gt;
 *       &lt;attribute name="receiveRequestAttemptId" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "receiveSqsMessage", propOrder = {
    "value",
    "file"
})
public class ReceiveSqsMessage {

    protected String value;
    protected String file;
    @XmlAttribute(name = "queue", required = true)
    protected String queue;
    @XmlAttribute(name = "maxNumberOfMessages")
    protected Integer maxNumberOfMessages;
    @XmlAttribute(name = "visibilityTimeout")
    protected Integer visibilityTimeout;
    @XmlAttribute(name = "waitTimeSeconds")
    protected Integer waitTimeSeconds;
    @XmlAttribute(name = "receiveRequestAttemptId")
    protected String receiveRequestAttemptId;

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
     * Gets the value of the queue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueue() {
        return queue;
    }

    /**
     * Sets the value of the queue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueue(String value) {
        this.queue = value;
    }

    /**
     * Gets the value of the maxNumberOfMessages property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxNumberOfMessages() {
        return maxNumberOfMessages;
    }

    /**
     * Sets the value of the maxNumberOfMessages property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxNumberOfMessages(Integer value) {
        this.maxNumberOfMessages = value;
    }

    /**
     * Gets the value of the visibilityTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getVisibilityTimeout() {
        return visibilityTimeout;
    }

    /**
     * Sets the value of the visibilityTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setVisibilityTimeout(Integer value) {
        this.visibilityTimeout = value;
    }

    /**
     * Gets the value of the waitTimeSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getWaitTimeSeconds() {
        return waitTimeSeconds;
    }

    /**
     * Sets the value of the waitTimeSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setWaitTimeSeconds(Integer value) {
        this.waitTimeSeconds = value;
    }

    /**
     * Gets the value of the receiveRequestAttemptId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceiveRequestAttemptId() {
        return receiveRequestAttemptId;
    }

    /**
     * Sets the value of the receiveRequestAttemptId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceiveRequestAttemptId(String value) {
        this.receiveRequestAttemptId = value;
    }

}
