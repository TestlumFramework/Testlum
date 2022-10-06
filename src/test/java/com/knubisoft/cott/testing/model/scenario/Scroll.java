
package com.knubisoft.cott.testing.model.scenario;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for scroll complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="scroll"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractCommand"&gt;
 *       &lt;attribute name="scrollType" type="{http://www.knubisoft.com/cott/testing/model/scenario}scrollType" default="page" /&gt;
 *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" /&gt;
 *       &lt;attribute name="direction" type="{http://www.knubisoft.com/cott/testing/model/scenario}scrollDirection" default="down" /&gt;
 *       &lt;attribute name="measure" type="{http://www.knubisoft.com/cott/testing/model/scenario}scrollMeasure" default="pixel" /&gt;
 *       &lt;attribute name="locator" type="{http://www.knubisoft.com/cott/testing/model/scenario}scenarioLocator" /&gt;
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
    extends AbstractCommand
{

    @XmlAttribute(name = "scrollType")
    protected ScrollType scrollType;
    @XmlAttribute(name = "value", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger value;
    @XmlAttribute(name = "direction")
    protected ScrollDirection direction;
    @XmlAttribute(name = "measure")
    protected ScrollMeasure measure;
    @XmlAttribute(name = "locator")
    protected String locator;

    /**
     * Gets the value of the scrollType property.
     * 
     * @return
     *     possible object is
     *     {@link ScrollType }
     *     
     */
    public ScrollType getScrollType() {
        if (scrollType == null) {
            return ScrollType.PAGE;
        } else {
            return scrollType;
        }
    }

    /**
     * Sets the value of the scrollType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScrollType }
     *     
     */
    public void setScrollType(ScrollType value) {
        this.scrollType = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setValue(BigInteger value) {
        this.value = value;
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

    /**
     * Gets the value of the locator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocator() {
        return locator;
    }

    /**
     * Sets the value of the locator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocator(String value) {
        this.locator = value;
    }

}
