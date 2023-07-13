
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
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}comparisonConditions"&gt;
 *       &lt;attribute name="locatorId" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}scenarioLocator" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "compareWithPart")
public class CompareWithPart
    extends ComparisonConditions
{

    @XmlAttribute(name = "locatorId", required = true)
    protected String locatorId;

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

}
