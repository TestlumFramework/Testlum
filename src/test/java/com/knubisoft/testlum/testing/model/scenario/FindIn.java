
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for findIn complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="findIn"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="fullScreen" type="{http://www.knubisoft.com/testlum/testing/model/scenario}findInFullScreen"/&gt;
 *         &lt;element name="element" type="{http://www.knubisoft.com/testlum/testing/model/scenario}findInElement"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "findIn", propOrder = {
    "fullScreen",
    "element"
})
public class FindIn {

    protected FindInFullScreen fullScreen;
    protected FindInElement element;

    /**
     * Gets the value of the fullScreen property.
     * 
     * @return
     *     possible object is
     *     {@link FindInFullScreen }
     *     
     */
    public FindInFullScreen getFullScreen() {
        return fullScreen;
    }

    /**
     * Sets the value of the fullScreen property.
     * 
     * @param value
     *     allowed object is
     *     {@link FindInFullScreen }
     *     
     */
    public void setFullScreen(FindInFullScreen value) {
        this.fullScreen = value;
    }

    /**
     * Gets the value of the element property.
     * 
     * @return
     *     possible object is
     *     {@link FindInElement }
     *     
     */
    public FindInElement getElement() {
        return element;
    }

    /**
     * Sets the value of the element property.
     * 
     * @param value
     *     allowed object is
     *     {@link FindInElement }
     *     
     */
    public void setElement(FindInElement value) {
        this.element = value;
    }

}
