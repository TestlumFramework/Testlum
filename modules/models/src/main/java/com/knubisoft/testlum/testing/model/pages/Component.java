
package com.knubisoft.testlum.testing.model.pages;

import jakarta.xml.bind.annotation.*;


/**
 * Component what contains locators to use in tests and can be included to pages
 * 
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="locators" type="{http://www.knubisoft.com/testlum/testing/model/pages}locators"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "locators"
})
@XmlRootElement(name = "component")
public class Component {

    @XmlElement(required = true)
    protected Locators locators;

    /**
     * Gets the value of the locators property.
     * 
     * @return
     *     possible object is
     *     {@link Locators }
     *     
     */
    public Locators getLocators() {
        return locators;
    }

    /**
     * Sets the value of the locators property.
     * 
     * @param value
     *     allowed object is
     *     {@link Locators }
     *     
     */
    public void setLocators(Locators value) {
        this.locators = value;
    }

}
