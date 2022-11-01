
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for combinedKeyAction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="combinedKeyAction"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}commandWithLocator"&gt;
 *       &lt;attribute name="combinedKeyCommand" type="{http://www.knubisoft.com/cott/testing/model/scenario}combinedKeyActionEnum" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "combinedKeyAction")
public class CombinedKeyAction
    extends CommandWithLocator
{

    @XmlAttribute(name = "combinedKeyCommand")
    protected CombinedKeyActionEnum combinedKeyCommand;

    /**
     * Gets the value of the combinedKeyCommand property.
     * 
     * @return
     *     possible object is
     *     {@link CombinedKeyActionEnum }
     *     
     */
    public CombinedKeyActionEnum getCombinedKeyCommand() {
        return combinedKeyCommand;
    }

    /**
     * Sets the value of the combinedKeyCommand property.
     * 
     * @param value
     *     allowed object is
     *     {@link CombinedKeyActionEnum }
     *     
     */
    public void setCombinedKeyCommand(CombinedKeyActionEnum value) {
        this.combinedKeyCommand = value;
    }

}
