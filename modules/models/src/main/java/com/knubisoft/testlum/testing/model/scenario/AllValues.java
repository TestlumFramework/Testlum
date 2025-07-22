
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for allValues complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="allValues"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="type" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}typeForAllValues" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "allValues")
public class AllValues {

    @XmlAttribute(name = "type", required = true)
    protected TypeForAllValues type;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link TypeForAllValues }
     *     
     */
    public TypeForAllValues getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeForAllValues }
     *     
     */
    public void setType(TypeForAllValues value) {
        this.type = value;
    }

}
