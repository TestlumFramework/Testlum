
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for swipeElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="swipeElement"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}commandWithLocator"&gt;
 *       &lt;attribute name="direction" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}swipeDirection" /&gt;
 *       &lt;attribute name="percent" type="{http://www.knubisoft.com/testlum/testing/model/scenario}percentsPattern" default="70" /&gt;
 *       &lt;attribute name="quantity" type="{http://www.w3.org/2001/XMLSchema}int" default="1" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "swipeElement")
public class SwipeElement
    extends CommandWithLocator
{

    @XmlAttribute(name = "direction", required = true)
    protected SwipeDirection direction;
    @XmlAttribute(name = "percent")
    protected Integer percent;
    @XmlAttribute(name = "quantity")
    protected Integer quantity;

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link SwipeDirection }
     *     
     */
    public SwipeDirection getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link SwipeDirection }
     *     
     */
    public void setDirection(SwipeDirection value) {
        this.direction = value;
    }

    /**
     * Gets the value of the percent property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getPercent() {
        if (percent == null) {
            return  70;
        } else {
            return percent;
        }
    }

    /**
     * Sets the value of the percent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPercent(Integer value) {
        this.percent = value;
    }

    /**
     * Gets the value of the quantity property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getQuantity() {
        if (quantity == null) {
            return  1;
        } else {
            return quantity;
        }
    }

    /**
     * Sets the value of the quantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setQuantity(Integer value) {
        this.quantity = value;
    }

}
