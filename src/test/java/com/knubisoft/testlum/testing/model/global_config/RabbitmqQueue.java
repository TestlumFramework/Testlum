
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for rabbitmqQueue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="rabbitmqQueue"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}rabbitmqQueueConfig"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="toExchange" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="withRoutingKey" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="destinationType" type="{http://www.knubisoft.com/testlum/testing/model/global-config}destinationType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rabbitmqQueue", propOrder = {
    "toExchange",
    "withRoutingKey",
    "destinationType"
})
public class RabbitmqQueue
    extends RabbitmqQueueConfig
{

    @XmlElement(required = true)
    protected String toExchange;
    @XmlElement(required = true)
    protected String withRoutingKey;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected DestinationType destinationType;

    /**
     * Gets the value of the toExchange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToExchange() {
        return toExchange;
    }

    /**
     * Sets the value of the toExchange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToExchange(String value) {
        this.toExchange = value;
    }

    /**
     * Gets the value of the withRoutingKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWithRoutingKey() {
        return withRoutingKey;
    }

    /**
     * Sets the value of the withRoutingKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWithRoutingKey(String value) {
        this.withRoutingKey = value;
    }

    /**
     * Gets the value of the destinationType property.
     * 
     * @return
     *     possible object is
     *     {@link DestinationType }
     *     
     */
    public DestinationType getDestinationType() {
        return destinationType;
    }

    /**
     * Sets the value of the destinationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link DestinationType }
     *     
     */
    public void setDestinationType(DestinationType value) {
        this.destinationType = value;
    }

}
