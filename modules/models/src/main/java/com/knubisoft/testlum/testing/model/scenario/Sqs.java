
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for sqs complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sqs"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;choice&gt;
 *           &lt;element name="send" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sendSqsMessage" maxOccurs="unbounded"/&gt;
 *           &lt;element name="receive" type="{http://www.knubisoft.com/testlum/testing/model/scenario}receiveSqsMessage" maxOccurs="unbounded"/&gt;
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
@XmlType(name = "sqs", propOrder = {
    "sendOrReceive"
})
public class Sqs
    extends AbstractCommand
{

    @XmlElements({
        @XmlElement(name = "send", type = SendSqsMessage.class),
        @XmlElement(name = "receive", type = ReceiveSqsMessage.class)
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
     * {@link SendSqsMessage }
     * {@link ReceiveSqsMessage }
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
