
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fromDate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fromDate"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="relative" type="{http://www.knubisoft.com/testlum/testing/model/scenario}dateRelative"/&gt;
 *         &lt;element name="specified" type="{http://www.knubisoft.com/testlum/testing/model/scenario}dateSpecified"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="format" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fromDate", propOrder = {
    "relative",
    "specified"
})
public class FromDate {

    protected DateRelative relative;
    protected DateSpecified specified;
    @XmlAttribute(name = "format", required = true)
    protected String format;

    /**
     * Gets the value of the relative property.
     * 
     * @return
     *     possible object is
     *     {@link DateRelative }
     *     
     */
    public DateRelative getRelative() {
        return relative;
    }

    /**
     * Sets the value of the relative property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateRelative }
     *     
     */
    public void setRelative(DateRelative value) {
        this.relative = value;
    }

    /**
     * Gets the value of the specified property.
     * 
     * @return
     *     possible object is
     *     {@link DateSpecified }
     *     
     */
    public DateSpecified getSpecified() {
        return specified;
    }

    /**
     * Sets the value of the specified property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateSpecified }
     *     
     */
    public void setSpecified(DateSpecified value) {
        this.specified = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormat(String value) {
        this.format = value;
    }

}
