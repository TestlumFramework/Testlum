
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for assertAlert complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="assertAlert"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="text" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="negative" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "assertAlert", propOrder = {
    "text"
})
public class AssertAlert
    extends AbstractUiCommand
{

    @XmlElement(required = true)
    protected String text;
    @XmlAttribute(name = "negative")
    protected Boolean negative;

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Gets the value of the negative property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNegative() {
        if (negative == null) {
            return false;
        } else {
            return negative;
        }
    }

    /**
     * Sets the value of the negative property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNegative(Boolean value) {
        this.negative = value;
    }

}
