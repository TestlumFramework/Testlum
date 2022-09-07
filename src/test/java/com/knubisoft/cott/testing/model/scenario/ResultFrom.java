
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for resultFrom complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="resultFrom"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="type" type="{http://www.knubisoft.com/cott/testing/model/scenario}varType" /&gt;
 *       &lt;attribute name="InputValue" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resultFrom")
public class ResultFrom {

    @XmlAttribute(name = "type")
    protected VarType type;
    @XmlAttribute(name = "InputValue")
    protected String inputValue;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link VarType }
     *     
     */
    public VarType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link VarType }
     *     
     */
    public void setType(VarType value) {
        this.type = value;
    }

    /**
     * Gets the value of the inputValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInputValue() {
        return inputValue;
    }

    /**
     * Sets the value of the inputValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInputValue(String value) {
        this.inputValue = value;
    }

}
