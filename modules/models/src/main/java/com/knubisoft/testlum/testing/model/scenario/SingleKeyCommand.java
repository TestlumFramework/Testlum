
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for singleKeyCommand complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="singleKeyCommand"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;attribute name="times" type="{http://www.knubisoft.com/testlum/testing/model/scenario}positiveIntegerMin1" default="1" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "singleKeyCommand")
@XmlSeeAlso({
    Tab.class,
    Enter.class,
    BackSpace.class,
    Escape.class,
    Space.class
})
public class SingleKeyCommand
    extends AbstractUiCommand
{

    @XmlAttribute(name = "times")
    protected Integer times;

    /**
     * Gets the value of the times property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getTimes() {
        if (times == null) {
            return  1;
        } else {
            return times;
        }
    }

    /**
     * Sets the value of the times property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTimes(Integer value) {
        this.times = value;
    }

}
