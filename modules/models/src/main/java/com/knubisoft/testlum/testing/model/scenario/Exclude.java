
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for exclude complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="exclude"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="byLocator" type="{http://www.knubisoft.com/testlum/testing/model/scenario}byLocator"/&gt;
 *         &lt;element name="byArea" type="{http://www.knubisoft.com/testlum/testing/model/scenario}byArea"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "exclude", propOrder = {
    "byLocator",
    "byArea"
})
public class Exclude {

    protected ByLocator byLocator;
    protected ByArea byArea;

    /**
     * Gets the value of the byLocator property.
     * 
     * @return
     *     possible object is
     *     {@link ByLocator }
     *     
     */
    public ByLocator getByLocator() {
        return byLocator;
    }

    /**
     * Sets the value of the byLocator property.
     * 
     * @param value
     *     allowed object is
     *     {@link ByLocator }
     *     
     */
    public void setByLocator(ByLocator value) {
        this.byLocator = value;
    }

    /**
     * Gets the value of the byArea property.
     * 
     * @return
     *     possible object is
     *     {@link ByArea }
     *     
     */
    public ByArea getByArea() {
        return byArea;
    }

    /**
     * Sets the value of the byArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link ByArea }
     *     
     */
    public void setByArea(ByArea value) {
        this.byArea = value;
    }

}
