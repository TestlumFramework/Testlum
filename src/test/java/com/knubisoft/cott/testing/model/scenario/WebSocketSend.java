
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for webSocketSend complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="webSocketSend"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="message" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString"/&gt;
 *           &lt;element name="file" type="{http://www.knubisoft.com/cott/testing/model/scenario}bodyFile"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="receive" type="{http://www.knubisoft.com/cott/testing/model/scenario}webSocketReceive" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="endpoint" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}endpointPattern" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webSocketSend", propOrder = {
    "message",
    "file",
    "receive"
})
public class WebSocketSend {

    protected String message;
    protected String file;
    protected WebSocketReceive receive;
    @XmlAttribute(name = "endpoint", required = true)
    protected String endpoint;

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
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
     * Gets the value of the receive property.
     * 
     * @return
     *     possible object is
     *     {@link WebSocketReceive }
     *     
     */
    public WebSocketReceive getReceive() {
        return receive;
    }

    /**
     * Sets the value of the receive property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebSocketReceive }
     *     
     */
    public void setReceive(WebSocketReceive value) {
        this.receive = value;
    }

    /**
     * Gets the value of the endpoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the value of the endpoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndpoint(String value) {
        this.endpoint = value;
    }

}
