
package com.knubisoft.cott.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for hotKey complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="hotKey"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="combinedKeyAction" type="{http://www.knubisoft.com/cott/testing/model/scenario}combinedKeyAction"/&gt;
 *         &lt;element name="singleKeyAction" type="{http://www.knubisoft.com/cott/testing/model/scenario}singleKeyAction"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hotKey", propOrder = {
    "combinedKeyActionOrSingleKeyAction"
})
public class HotKey
    extends AbstractCommand
{

    @XmlElements({
        @XmlElement(name = "combinedKeyAction", type = CombinedKeyAction.class),
        @XmlElement(name = "singleKeyAction", type = SingleKeyAction.class)
    })
    protected List<AbstractCommand> combinedKeyActionOrSingleKeyAction;

    /**
     * Gets the value of the combinedKeyActionOrSingleKeyAction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the combinedKeyActionOrSingleKeyAction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCombinedKeyActionOrSingleKeyAction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CombinedKeyAction }
     * {@link SingleKeyAction }
     * 
     * 
     */
    public List<AbstractCommand> getCombinedKeyActionOrSingleKeyAction() {
        if (combinedKeyActionOrSingleKeyAction == null) {
            combinedKeyActionOrSingleKeyAction = new ArrayList<AbstractCommand>();
        }
        return this.combinedKeyActionOrSingleKeyAction;
    }

}
