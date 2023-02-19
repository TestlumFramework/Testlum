
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for scroll complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="scroll"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}commandWithOptionalLocator"&gt;
 *       &lt;attribute name="type" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}scrollType" /&gt;
 *       &lt;attribute name="direction" type="{http://www.knubisoft.com/cott/testing/model/scenario}scrollDirection" default="down" /&gt;
 *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="measure" type="{http://www.knubisoft.com/cott/testing/model/scenario}scrollMeasure" default="pixel" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "scroll")
public class Scroll
    extends CommandWithOptionalLocator
{

    @XmlAttribute(name = "type", required = true)
    protected ScrollType type;
    @XmlAttribute(name = "direction")
    protected ScrollDirection direction;
    @XmlAttribute(name = "value", required = true)
    protected int value;
    @XmlAttribute(name = "measure")
    protected ScrollMeasure measure;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link ScrollType }
     *     
     */
    public ScrollType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScrollType }
     *     
     */
    public void setType(ScrollType value) {
        this.type = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link ScrollDirection }
     *     
     */
    public ScrollDirection getDirection() {
        if (direction == null) {
            return ScrollDirection.DOWN;
        } else {
            return direction;
        }
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScrollDirection }
     *     
     */
    public void setDirection(ScrollDirection value) {
        this.direction = value;
    }

    /**
     * Gets the value of the value property.
     * 
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Gets the value of the measure property.
     * 
     * @return
     *     possible object is
     *     {@link ScrollMeasure }
     *     
     */
    public ScrollMeasure getMeasure() {
        if (measure == null) {
            return ScrollMeasure.PIXEL;
        } else {
            return measure;
        }
    }

    /**
     * Sets the value of the measure property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScrollMeasure }
     *     
     */
    public void setMeasure(ScrollMeasure value) {
        this.measure = value;
    }

}
