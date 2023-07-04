
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for singleKeyCommand complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="singleKeyCommand"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;attribute name="repeat" type="{http://www.knubisoft.com/testlum/testing/model/scenario}positiveIntegerMin1" default="1" /&gt;
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

    @XmlAttribute(name = "repeat")
    protected Integer repeat;

    /**
     * Gets the value of the repeat property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getRepeat() {
        if (repeat == null) {
            return  1;
        } else {
            return repeat;
        }
    }

    /**
     * Sets the value of the repeat property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRepeat(Integer value) {
        this.repeat = value;
    }

}
