
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for websocketStomp complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="websocketStomp"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="subscribe" type="{http://www.knubisoft.com/testlum/testing/model/scenario}websocketSubscribe"/&gt;
 *         &lt;element name="send" type="{http://www.knubisoft.com/testlum/testing/model/scenario}websocketSend"/&gt;
 *         &lt;element name="receive" type="{http://www.knubisoft.com/testlum/testing/model/scenario}websocketReceive"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "websocketStomp", propOrder = {
    "subscribeOrSendOrReceive"
})
public class WebsocketStomp {

    @XmlElements({
        @XmlElement(name = "subscribe", type = WebsocketSubscribe.class),
        @XmlElement(name = "send", type = WebsocketSend.class),
        @XmlElement(name = "receive", type = WebsocketReceive.class)
    })
    protected List<Object> subscribeOrSendOrReceive;

    /**
     * Gets the value of the subscribeOrSendOrReceive property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subscribeOrSendOrReceive property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubscribeOrSendOrReceive().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WebsocketSubscribe }
     * {@link WebsocketSend }
     * {@link WebsocketReceive }
     * 
     * 
     */
    public List<Object> getSubscribeOrSendOrReceive() {
        if (subscribeOrSendOrReceive == null) {
            subscribeOrSendOrReceive = new ArrayList<Object>();
        }
        return this.subscribeOrSendOrReceive;
    }

}
