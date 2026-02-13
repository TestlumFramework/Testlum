
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
 *         &lt;element name="constant" type="{http://www.knubisoft.com/testlum/testing/model/scenario}fromDateConstant"/&gt;
 *         &lt;element name="now" type="{http://www.knubisoft.com/testlum/testing/model/scenario}now"/&gt;
 *         &lt;element name="beforeNow" type="{http://www.knubisoft.com/testlum/testing/model/scenario}dateShift"/&gt;
 *         &lt;element name="afterNow" type="{http://www.knubisoft.com/testlum/testing/model/scenario}dateShift"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="format" type="{http://www.w3.org/2001/XMLSchema}string" default="yyyy-MM-dd HH:mm:ss" /&gt;
 *       &lt;attribute name="timezone" type="{http://www.knubisoft.com/testlum/testing/model/scenario}timezoneType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fromDate", propOrder = {
    "constant",
    "now",
    "beforeNow",
    "afterNow"
})
public class FromDate {

    protected FromDateConstant constant;
    protected Now now;
    protected DateShift beforeNow;
    protected DateShift afterNow;
    @XmlAttribute(name = "format")
    protected String format;
    @XmlAttribute(name = "timezone")
    protected String timezone;

    /**
     * Gets the value of the constant property.
     * 
     * @return
     *     possible object is
     *     {@link FromDateConstant }
     *     
     */
    public FromDateConstant getConstant() {
        return constant;
    }

    /**
     * Sets the value of the constant property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromDateConstant }
     *     
     */
    public void setConstant(FromDateConstant value) {
        this.constant = value;
    }

    /**
     * Gets the value of the now property.
     * 
     * @return
     *     possible object is
     *     {@link Now }
     *     
     */
    public Now getNow() {
        return now;
    }

    /**
     * Sets the value of the now property.
     * 
     * @param value
     *     allowed object is
     *     {@link Now }
     *     
     */
    public void setNow(Now value) {
        this.now = value;
    }

    /**
     * Gets the value of the beforeNow property.
     * 
     * @return
     *     possible object is
     *     {@link DateShift }
     *     
     */
    public DateShift getBeforeNow() {
        return beforeNow;
    }

    /**
     * Sets the value of the beforeNow property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateShift }
     *     
     */
    public void setBeforeNow(DateShift value) {
        this.beforeNow = value;
    }

    /**
     * Gets the value of the afterNow property.
     * 
     * @return
     *     possible object is
     *     {@link DateShift }
     *     
     */
    public DateShift getAfterNow() {
        return afterNow;
    }

    /**
     * Sets the value of the afterNow property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateShift }
     *     
     */
    public void setAfterNow(DateShift value) {
        this.afterNow = value;
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
        if (format == null) {
            return "yyyy-MM-dd HH:mm:ss";
        } else {
            return format;
        }
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

    /**
     * Gets the value of the timezone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Sets the value of the timezone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimezone(String value) {
        this.timezone = value;
    }

}
