
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for oneValue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="oneValue"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="type" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}typeForOneValue" /&gt;
 *       &lt;attribute name="by" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}selectOrDeselectBy" /&gt;
 *       &lt;attribute name="value" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "oneValue")
public class OneValue {

    @XmlAttribute(name = "type", required = true)
    protected TypeForOneValue type;
    @XmlAttribute(name = "by", required = true)
    protected SelectOrDeselectBy by;
    @XmlAttribute(name = "value", required = true)
    protected String value;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link TypeForOneValue }
     *     
     */
    public TypeForOneValue getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeForOneValue }
     *     
     */
    public void setType(TypeForOneValue value) {
        this.type = value;
    }

    /**
     * Gets the value of the by property.
     * 
     * @return
     *     possible object is
     *     {@link SelectOrDeselectBy }
     *     
     */
    public SelectOrDeselectBy getBy() {
        return by;
    }

    /**
     * Sets the value of the by property.
     * 
     * @param value
     *     allowed object is
     *     {@link SelectOrDeselectBy }
     *     
     */
    public void setBy(SelectOrDeselectBy value) {
        this.by = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

}
