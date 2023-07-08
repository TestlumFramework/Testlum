
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for compareWith complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="compareWith"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="fullScreen" type="{http://www.knubisoft.com/testlum/testing/model/scenario}compareWithFullScreen"/&gt;
 *         &lt;element name="image" type="{http://www.knubisoft.com/testlum/testing/model/scenario}compareWithImage"/&gt;
 *         &lt;element name="element" type="{http://www.knubisoft.com/testlum/testing/model/scenario}compareWithElement"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "compareWith", propOrder = {
    "fullScreen",
    "image",
    "element"
})
public class CompareWith {

    protected CompareWithFullScreen fullScreen;
    protected CompareWithImage image;
    protected CompareWithElement element;

    /**
     * Gets the value of the fullScreen property.
     * 
     * @return
     *     possible object is
     *     {@link CompareWithFullScreen }
     *     
     */
    public CompareWithFullScreen getFullScreen() {
        return fullScreen;
    }

    /**
     * Sets the value of the fullScreen property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompareWithFullScreen }
     *     
     */
    public void setFullScreen(CompareWithFullScreen value) {
        this.fullScreen = value;
    }

    /**
     * Gets the value of the image property.
     * 
     * @return
     *     possible object is
     *     {@link CompareWithImage }
     *     
     */
    public CompareWithImage getImage() {
        return image;
    }

    /**
     * Sets the value of the image property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompareWithImage }
     *     
     */
    public void setImage(CompareWithImage value) {
        this.image = value;
    }

    /**
     * Gets the value of the element property.
     * 
     * @return
     *     possible object is
     *     {@link CompareWithElement }
     *     
     */
    public CompareWithElement getElement() {
        return element;
    }

    /**
     * Sets the value of the element property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompareWithElement }
     *     
     */
    public void setElement(CompareWithElement value) {
        this.element = value;
    }

}
