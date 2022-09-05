
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dropDown complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dropDown"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}commandWithLocator"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="oneValue" type="{http://www.knubisoft.com/cott/testing/model/scenario}oneValue"/&gt;
 *         &lt;element name="allValues" type="{http://www.knubisoft.com/cott/testing/model/scenario}allValues"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dropDown", propOrder = {
    "oneValue",
    "allValues"
})
public class DropDown
    extends CommandWithLocator
{

    protected OneValue oneValue;
    protected AllValues allValues;

    /**
     * Gets the value of the oneValue property.
     * 
     * @return
     *     possible object is
     *     {@link OneValue }
     *     
     */
    public OneValue getOneValue() {
        return oneValue;
    }

    /**
     * Sets the value of the oneValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link OneValue }
     *     
     */
    public void setOneValue(OneValue value) {
        this.oneValue = value;
    }

    /**
     * Gets the value of the allValues property.
     * 
     * @return
     *     possible object is
     *     {@link AllValues }
     *     
     */
    public AllValues getAllValues() {
        return allValues;
    }

    /**
     * Sets the value of the allValues property.
     * 
     * @param value
     *     allowed object is
     *     {@link AllValues }
     *     
     */
    public void setAllValues(AllValues value) {
        this.allValues = value;
    }

}
