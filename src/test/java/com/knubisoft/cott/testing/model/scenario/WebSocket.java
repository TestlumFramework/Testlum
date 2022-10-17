
package com.knubisoft.cott.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for webSocket complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="webSocket"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractCommand"&gt;
 *       &lt;sequence maxOccurs="unbounded"&gt;
 *         &lt;choice&gt;
 *           &lt;element name="send" type="{http://www.knubisoft.com/cott/testing/model/scenario}webSocketSend" maxOccurs="unbounded"/&gt;
 *           &lt;element name="receive" type="{http://www.knubisoft.com/cott/testing/model/scenario}webSocketReceive" maxOccurs="unbounded"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}aliasPattern" /&gt;
 *       &lt;attribute name="topics" type="{http://www.knubisoft.com/cott/testing/model/scenario}endpointPattern" /&gt;
 *       &lt;attribute name="disconnect" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webSocket", propOrder = {
    "sendOrReceive"
})
public class WebSocket
    extends AbstractCommand
{

    @XmlElements({
        @XmlElement(name = "send", type = WebSocketSend.class),
        @XmlElement(name = "receive", type = WebSocketReceive.class)
    })
    protected List<Object> sendOrReceive;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;
    @XmlAttribute(name = "topics")
    protected String topics;
    @XmlAttribute(name = "disconnect")
    protected Boolean disconnect;

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
     * {@link WebSocketSend }
     * {@link WebSocketReceive }
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

    /**
     * Gets the value of the topics property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTopics() {
        return topics;
    }

    /**
     * Sets the value of the topics property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTopics(String value) {
        this.topics = value;
    }

    /**
     * Gets the value of the disconnect property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDisconnect() {
        if (disconnect == null) {
            return true;
        } else {
            return disconnect;
        }
    }

    /**
     * Sets the value of the disconnect property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDisconnect(Boolean value) {
        this.disconnect = value;
    }

}
