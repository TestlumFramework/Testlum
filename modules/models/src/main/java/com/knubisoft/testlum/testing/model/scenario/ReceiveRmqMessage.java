
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for receiveRmqMessage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="receiveRmqMessage"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="value" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="file" type="{http://www.knubisoft.com/testlum/testing/model/scenario}expectedPattern"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="queue" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="prefetchCount" type="{http://www.w3.org/2001/XMLSchema}int" default="1" /&gt;
 *       &lt;attribute name="timeoutMillis" type="{http://www.w3.org/2001/XMLSchema}long" default="1000" /&gt;
 *       &lt;attribute name="headers" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "receiveRmqMessage", propOrder = {
    "value",
    "file"
})
public class ReceiveRmqMessage {

    protected String value;
    protected String file;
    @XmlAttribute(name = "queue", required = true)
    protected String queue;
    @XmlAttribute(name = "prefetchCount")
    protected Integer prefetchCount;
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
     * Gets the value of the prefetchCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getPrefetchCount() {
        if (prefetchCount == null) {
            return  1;
        } else {
            return prefetchCount;
        }
    }

    /**
     * Sets the value of the prefetchCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPrefetchCount(Integer value) {
        this.prefetchCount = value;
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
            return  1000L;
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
