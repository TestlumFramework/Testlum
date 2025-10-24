
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for hover complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="hover"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}commandWithLocator"&gt;
 *       &lt;attribute name="moveToEmptySpace" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hover")
public class Hover
    extends CommandWithLocator
{

    @XmlAttribute(name = "moveToEmptySpace")
    protected Boolean moveToEmptySpace;

    /**
     * Gets the value of the moveToEmptySpace property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMoveToEmptySpace() {
        if (moveToEmptySpace == null) {
            return false;
        } else {
            return moveToEmptySpace;
        }
    }

    /**
     * Sets the value of the moveToEmptySpace property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMoveToEmptySpace(Boolean value) {
        this.moveToEmptySpace = value;
    }

}
