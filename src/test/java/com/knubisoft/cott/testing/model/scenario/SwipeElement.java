
package com.knubisoft.cott.testing.model.scenario;

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
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}commandWithLocator"&gt;
 *       &lt;attribute name="direction" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}swipeElementDirection" /&gt;
 *       &lt;attribute name="valueInPercents" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}percentsPattern" /&gt;
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
    protected SwipeElementDirection direction;
    @XmlAttribute(name = "valueInPercents", required = true)
    protected int valueInPercents;

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link SwipeElementDirection }
     *     
     */
    public SwipeElementDirection getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link SwipeElementDirection }
     *     
     */
    public void setDirection(SwipeElementDirection value) {
        this.direction = value;
    }

    /**
     * Gets the value of the valueInPercents property.
     * 
     */
    public int getValueInPercents() {
        return valueInPercents;
    }

    /**
     * Sets the value of the valueInPercents property.
     * 
     */
    public void setValueInPercents(int value) {
        this.valueInPercents = value;
    }

}
