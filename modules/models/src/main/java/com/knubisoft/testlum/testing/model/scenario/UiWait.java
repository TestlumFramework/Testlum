
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for uiWait complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="uiWait"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice minOccurs="0"&gt;
 *         &lt;element name="clickable" type="{http://www.knubisoft.com/testlum/testing/model/scenario}clickable"/&gt;
 *         &lt;element name="visible" type="{http://www.knubisoft.com/testlum/testing/model/scenario}visible"/&gt;
 *       &lt;/choice&gt;
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
@XmlType(name = "uiWait", propOrder = {
    "clickable",
    "visible"
})
public class UiWait
    extends AbstractUiCommand
{

    protected Clickable clickable;
    protected Visible visible;
    @XmlAttribute(name = "time", required = true)
    protected String time;
    @XmlAttribute(name = "unit")
    protected Timeunit unit;

    /**
     * Gets the value of the clickable property.
     * 
     * @return
     *     possible object is
     *     {@link Clickable }
     *     
     */
    public Clickable getClickable() {
        return clickable;
    }

    /**
     * Sets the value of the clickable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Clickable }
     *     
     */
    public void setClickable(Clickable value) {
        this.clickable = value;
    }

    /**
     * Gets the value of the visible property.
     * 
     * @return
     *     possible object is
     *     {@link Visible }
     *     
     */
    public Visible getVisible() {
        return visible;
    }

    /**
     * Sets the value of the visible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Visible }
     *     
     */
    public void setVisible(Visible value) {
        this.visible = value;
    }

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
