
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sesBody complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sesBody"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="html" type="{http://www.knubisoft.com/cott/testing/model/scenario}sesTextContent"/&gt;
 *         &lt;element name="text" type="{http://www.knubisoft.com/cott/testing/model/scenario}sesTextContent"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sesBody", propOrder = {
    "html",
    "text"
})
public class SesBody {

    @XmlElement(required = true)
    protected SesTextContent html;
    @XmlElement(required = true)
    protected SesTextContent text;

    /**
     * Gets the value of the html property.
     * 
     * @return
     *     possible object is
     *     {@link SesTextContent }
     *     
     */
    public SesTextContent getHtml() {
        return html;
    }

    /**
     * Sets the value of the html property.
     * 
     * @param value
     *     allowed object is
     *     {@link SesTextContent }
     *     
     */
    public void setHtml(SesTextContent value) {
        this.html = value;
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link SesTextContent }
     *     
     */
    public SesTextContent getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link SesTextContent }
     *     
     */
    public void setText(SesTextContent value) {
        this.text = value;
    }

}
