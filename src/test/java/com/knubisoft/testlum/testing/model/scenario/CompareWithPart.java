
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for compareWithPart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="compareWithPart"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="locatorId" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}scenarioLocator" /&gt;
 *       &lt;attribute name="percentage" type="{http://www.knubisoft.com/testlum/testing/model/scenario}positiveDoubleMin0Max100" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "compareWithPart")
public class CompareWithPart {

    @XmlAttribute(name = "locatorId", required = true)
    protected String locatorId;
    @XmlAttribute(name = "percentage")
    protected Double percentage;

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
     * Gets the value of the percentage property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPercentage() {
        return percentage;
    }

    /**
     * Sets the value of the percentage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setPercentage(Double value) {
        this.percentage = value;
    }

}
