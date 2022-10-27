
package com.knubisoft.cott.testing.model.web.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;


/**
 * <p>Java class for newUi complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="newUi"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="commandOne" type="{http://www.knubisoft.com/cott/testing/model/web/scenario}commandOne"/&gt;
 *         &lt;element name="commandTwo" type="{http://www.knubisoft.com/cott/testing/model/web/scenario}commandTwo"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "newUi", propOrder = {
    "commandOneOrCommandTwo"
})
public class NewUi
    extends AbstractCommand
{

    @XmlElements({
        @XmlElement(name = "commandOne", type = CommandOne.class),
        @XmlElement(name = "commandTwo", type = CommandTwo.class)
    })
    protected List<Object> commandOneOrCommandTwo;

    /**
     * Gets the value of the commandOneOrCommandTwo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the commandOneOrCommandTwo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCommandOneOrCommandTwo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CommandOne }
     * {@link CommandTwo }
     * 
     * 
     */
    public List<Object> getCommandOneOrCommandTwo() {
        if (commandOneOrCommandTwo == null) {
            commandOneOrCommandTwo = new ArrayList<Object>();
        }
        return this.commandOneOrCommandTwo;
    }

}
