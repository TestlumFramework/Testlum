
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for wait complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="wait"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;attribute name="time" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}timePattern" /&gt;
 *       &lt;attribute name="unit" type="{http://www.knubisoft.com/testlum/testing/model/scenario}timeunit" default="seconds" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wait")
public class Wait
    extends AbstractCommand
{

    @XmlAttribute(name = "time", required = true)
    protected String time;
    @XmlAttribute(name = "unit")
    protected Timeunit unit;

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTime(String value) {
        this.time = value;
    }

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link Timeunit }
     *     
     */
    public Timeunit getUnit() {
        if (unit == null) {
            return Timeunit.SECONDS;
        } else {
            return unit;
        }
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Timeunit }
     *     
     */
    public void setUnit(Timeunit value) {
        this.unit = value;
    }

}
