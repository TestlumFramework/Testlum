
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sesMessage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sesMessage"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="body" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sesBody"/&gt;
 *         &lt;element name="subject" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sesTextContent"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sesMessage", propOrder = {
    "body",
    "subject"
})
public class SesMessage {

    @XmlElement(required = true)
    protected SesBody body;
    @XmlElement(required = true)
    protected SesTextContent subject;

    /**
     * Gets the value of the body property.
     * 
     * @return
     *     possible object is
     *     {@link SesBody }
     *     
     */
    public SesBody getBody() {
        return body;
    }

    /**
     * Sets the value of the body property.
     * 
     * @param value
     *     allowed object is
     *     {@link SesBody }
     *     
     */
    public void setBody(SesBody value) {
        this.body = value;
    }

    /**
     * Gets the value of the subject property.
     * 
     * @return
     *     possible object is
     *     {@link SesTextContent }
     *     
     */
    public SesTextContent getSubject() {
        return subject;
    }

    /**
     * Sets the value of the subject property.
     * 
     * @param value
     *     allowed object is
     *     {@link SesTextContent }
     *     
     */
    public void setSubject(SesTextContent value) {
        this.subject = value;
    }

}
