
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for kafka complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="kafka"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}storageIntegration"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="bootstrapAddress" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="autoOffsetReset" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="maxPollRecords" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="maxPollIntervalMs" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="clientId" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="groupId" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="autoCommitTimeout" type="{http://www.knubisoft.com/testlum/testing/model/global-config}positiveIntegerMin1" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "kafka", propOrder = {
    "bootstrapAddress",
    "autoOffsetReset",
    "maxPollRecords",
    "maxPollIntervalMs",
    "clientId",
    "groupId",
    "autoCommitTimeout"
})
public class Kafka
    extends StorageIntegration
{

    @XmlElement(required = true)
    protected String bootstrapAddress;
    @XmlElement(required = true)
    protected String autoOffsetReset;
    protected int maxPollRecords;
    protected int maxPollIntervalMs;
    @XmlElement(required = true)
    protected String clientId;
    @XmlElement(required = true)
    protected String groupId;
    protected Integer autoCommitTimeout;

    /**
     * Gets the value of the bootstrapAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBootstrapAddress() {
        return bootstrapAddress;
    }

    /**
     * Sets the value of the bootstrapAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBootstrapAddress(String value) {
        this.bootstrapAddress = value;
    }

    /**
     * Gets the value of the autoOffsetReset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    /**
     * Sets the value of the autoOffsetReset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAutoOffsetReset(String value) {
        this.autoOffsetReset = value;
    }

    /**
     * Gets the value of the maxPollRecords property.
     * 
     */
    public int getMaxPollRecords() {
        return maxPollRecords;
    }

    /**
     * Sets the value of the maxPollRecords property.
     * 
     */
    public void setMaxPollRecords(int value) {
        this.maxPollRecords = value;
    }

    /**
     * Gets the value of the maxPollIntervalMs property.
     * 
     */
    public int getMaxPollIntervalMs() {
        return maxPollIntervalMs;
    }

    /**
     * Sets the value of the maxPollIntervalMs property.
     * 
     */
    public void setMaxPollIntervalMs(int value) {
        this.maxPollIntervalMs = value;
    }

    /**
     * Gets the value of the clientId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the value of the clientId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientId(String value) {
        this.clientId = value;
    }

    /**
     * Gets the value of the groupId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the value of the groupId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupId(String value) {
        this.groupId = value;
    }

    /**
     * Gets the value of the autoCommitTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAutoCommitTimeout() {
        return autoCommitTimeout;
    }

    /**
     * Sets the value of the autoCommitTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAutoCommitTimeout(Integer value) {
        this.autoCommitTimeout = value;
    }

}
