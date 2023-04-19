
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for scrollNative complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="scrollNative"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}commandWithOptionalLocator"&gt;
 *       &lt;attribute name="type" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}scrollType" /&gt;
 *       &lt;attribute name="direction" type="{http://www.knubisoft.com/testlum/testing/model/scenario}scrollDirection" default="down" /&gt;
 *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "scrollNative")
public class ScrollNative
    extends CommandWithOptionalLocator
{

    @XmlAttribute(name = "type", required = true)
    protected ScrollType type;
    @XmlAttribute(name = "direction")
    protected ScrollDirection direction;
    @XmlAttribute(name = "value", required = true)
    protected int value;

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

}
