
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for compareWithImage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="compareWithImage"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="locatorId" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}scenarioLocator" /&gt;
 *       &lt;attribute name="attribute" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" default="src" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "compareWithImage")
public class CompareWithImage {

    @XmlAttribute(name = "locatorId", required = true)
    protected String locatorId;
    @XmlAttribute(name = "attribute")
    protected String attribute;

    /**
     * Gets the value of the locatorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocatorId() {
        return locatorId;
    }

    /**
     * Sets the value of the locatorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocatorId(String value) {
        this.locatorId = value;
    }

    /**
     * Gets the value of the attribute property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttribute() {
        if (attribute == null) {
            return "src";
        } else {
            return attribute;
        }
    }

    /**
     * Sets the value of the attribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttribute(String value) {
        this.attribute = value;
    }

}
