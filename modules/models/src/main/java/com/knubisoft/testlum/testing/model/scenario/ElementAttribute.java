
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for elementAttribute complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="elementAttribute"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webAttributeNamePattern" /&gt;
 *       &lt;attribute name="locator" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="locatorStrategy" type="{http://www.knubisoft.com/testlum/testing/model/scenario}locatorStrategy" default="locatorId" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "elementAttribute")
public class ElementAttribute {

    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "locator", required = true)
    protected String locator;
    @XmlAttribute(name = "locatorStrategy")
    protected LocatorStrategy locatorStrategy;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the locator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocator() {
        return locator;
    }

    /**
     * Sets the value of the locator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocator(String value) {
        this.locator = value;
    }

    /**
     * Gets the value of the locatorStrategy property.
     * 
     * @return
     *     possible object is
     *     {@link LocatorStrategy }
     *     
     */
    public LocatorStrategy getLocatorStrategy() {
        if (locatorStrategy == null) {
            return LocatorStrategy.LOCATOR_ID;
        } else {
            return locatorStrategy;
        }
    }

    /**
     * Sets the value of the locatorStrategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocatorStrategy }
     *     
     */
    public void setLocatorStrategy(LocatorStrategy value) {
        this.locatorStrategy = value;
    }

}
