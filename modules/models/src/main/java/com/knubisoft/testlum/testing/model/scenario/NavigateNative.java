
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for navigateNative complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="navigateNative"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;attribute name="destination" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}navigateNativeDestination" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "navigateNative")
public class NavigateNative
    extends AbstractUiCommand
{

    @XmlAttribute(name = "destination", required = true)
    protected NavigateNativeDestination destination;

    /**
     * Gets the value of the destination property.
     * 
     * @return
     *     possible object is
     *     {@link NavigateNativeDestination }
     *     
     */
    public NavigateNativeDestination getDestination() {
        return destination;
    }

    /**
     * Sets the value of the destination property.
     * 
     * @param value
     *     allowed object is
     *     {@link NavigateNativeDestination }
     *     
     */
    public void setDestination(NavigateNativeDestination value) {
        this.destination = value;
    }

}
