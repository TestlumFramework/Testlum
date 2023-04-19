
package com.knubisoft.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for kafka complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="kafka"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;choice&gt;
 *           &lt;element name="send" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sendKafkaMessage" maxOccurs="unbounded"/&gt;
 *           &lt;element name="receive" type="{http://www.knubisoft.com/testlum/testing/model/scenario}receiveKafkaMessage" maxOccurs="unbounded"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}aliasPattern" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "kafka", propOrder = {
    "sendOrReceive"
})
public class Kafka
    extends AbstractCommand
{

    @XmlElements({
        @XmlElement(name = "send", type = SendKafkaMessage.class),
        @XmlElement(name = "receive", type = ReceiveKafkaMessage.class)
    })
    protected List<Object> sendOrReceive;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the sendOrReceive property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sendOrReceive property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSendOrReceive().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SendKafkaMessage }
     * {@link ReceiveKafkaMessage }
     * 
     * 
     */
    public List<Object> getSendOrReceive() {
        if (sendOrReceive == null) {
            sendOrReceive = new ArrayList<Object>();
        }
        return this.sendOrReceive;
    }

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
    }

}
