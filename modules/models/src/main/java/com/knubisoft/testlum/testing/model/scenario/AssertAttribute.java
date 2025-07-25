
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for assertAttribute complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="assertAttribute"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="content" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webAttributeNamePattern" /&gt;
 *       &lt;attribute name="negative" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="locator" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="locatorStrategy" type="{http://www.knubisoft.com/testlum/testing/model/scenario}locatorStrategy" default="locatorId" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "assertAttribute", propOrder = {
    "content"
})
public class AssertAttribute
    extends AbstractUiCommand
{

    @XmlElement(required = true)
    protected String content;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "negative")
    protected Boolean negative;
    @XmlAttribute(name = "locator", required = true)
    protected String locator;
    @XmlAttribute(name = "locatorStrategy")
    protected LocatorStrategy locatorStrategy;

    /**
     * Gets the value of the content property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the value of the content property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContent(String value) {
        this.content = value;
    }

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
