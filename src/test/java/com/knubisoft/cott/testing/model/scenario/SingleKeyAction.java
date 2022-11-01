
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for singleKeyAction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="singleKeyAction"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractCommand"&gt;
 *       &lt;attribute name="singleKeyCommand" type="{http://www.knubisoft.com/cott/testing/model/scenario}singleKeyActionEnum" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "singleKeyAction")
public class SingleKeyAction
    extends AbstractCommand
{

    @XmlAttribute(name = "singleKeyCommand")
    protected SingleKeyActionEnum singleKeyCommand;

    /**
     * Gets the value of the singleKeyCommand property.
     * 
     * @return
     *     possible object is
     *     {@link SingleKeyActionEnum }
     *     
     */
    public SingleKeyActionEnum getSingleKeyCommand() {
        return singleKeyCommand;
    }

    /**
     * Sets the value of the singleKeyCommand property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleKeyActionEnum }
     *     
     */
    public void setSingleKeyCommand(SingleKeyActionEnum value) {
        this.singleKeyCommand = value;
    }

}
