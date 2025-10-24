
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fromElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fromElement"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="present" type="{http://www.knubisoft.com/testlum/testing/model/scenario}elementPresent"/&gt;
 *         &lt;element name="attribute" type="{http://www.knubisoft.com/testlum/testing/model/scenario}elementAttribute"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fromElement", propOrder = {
    "present",
    "attribute"
})
public class FromElement {

    protected ElementPresent present;
    protected ElementAttribute attribute;

    /**
     * Gets the value of the present property.
     * 
     * @return
     *     possible object is
     *     {@link ElementPresent }
     *     
     */
    public ElementPresent getPresent() {
        return present;
    }

    /**
     * Sets the value of the present property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElementPresent }
     *     
     */
    public void setPresent(ElementPresent value) {
        this.present = value;
    }

    /**
     * Gets the value of the attribute property.
     * 
     * @return
     *     possible object is
     *     {@link ElementAttribute }
     *     
     */
    public ElementAttribute getAttribute() {
        return attribute;
    }

    /**
     * Sets the value of the attribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElementAttribute }
     *     
     */
    public void setAttribute(ElementAttribute value) {
        this.attribute = value;
    }

}
