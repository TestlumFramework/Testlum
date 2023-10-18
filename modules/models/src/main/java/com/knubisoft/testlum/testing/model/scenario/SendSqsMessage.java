
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sendSqsMessage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendSqsMessage"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="value" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="file" type="{http://www.knubisoft.com/testlum/testing/model/scenario}bodyFile"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="queue" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="delaySeconds" type="{http://www.knubisoft.com/testlum/testing/model/scenario}positiveIntegerMin1" /&gt;
 *       &lt;attribute name="messageDeduplicationId" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="messageGroupId" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendSqsMessage", propOrder = {
    "value",
    "file"
})
public class SendSqsMessage {

    protected String value;
    protected String file;
    @XmlAttribute(name = "queue", required = true)
    protected String queue;
    @XmlAttribute(name = "delaySeconds")
    protected Integer delaySeconds;
    @XmlAttribute(name = "messageDeduplicationId")
    protected String messageDeduplicationId;
    @XmlAttribute(name = "messageGroupId")
    protected String messageGroupId;

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
     * Gets the value of the delaySeconds property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDelaySeconds() {
        return delaySeconds;
    }

    /**
     * Sets the value of the delaySeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDelaySeconds(Integer value) {
        this.delaySeconds = value;
    }

    /**
     * Gets the value of the messageDeduplicationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageDeduplicationId() {
        return messageDeduplicationId;
    }

    /**
     * Sets the value of the messageDeduplicationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageDeduplicationId(String value) {
        this.messageDeduplicationId = value;
    }

    /**
     * Gets the value of the messageGroupId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageGroupId() {
        return messageGroupId;
    }

    /**
     * Sets the value of the messageGroupId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageGroupId(String value) {
        this.messageGroupId = value;
    }

}
