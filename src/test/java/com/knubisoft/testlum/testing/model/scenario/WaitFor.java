
package com.knubisoft.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for waitFor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="waitFor"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="clickable" type="{http://www.knubisoft.com/testlum/testing/model/scenario}clickable"/&gt;
 *         &lt;element name="visible" type="{http://www.knubisoft.com/testlum/testing/model/scenario}visible"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "waitFor", propOrder = {
    "clickableOrVisible"
})
public class WaitFor
    extends AbstractUiCommand
{

    @XmlElements({
        @XmlElement(name = "clickable", type = Clickable.class),
        @XmlElement(name = "visible", type = Visible.class)
    })
    protected List<CommandWithLocator> clickableOrVisible;

    /**
     * Gets the value of the clickableOrVisible property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clickableOrVisible property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClickableOrVisible().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Clickable }
     * {@link Visible }
     * 
     * 
     */
    public List<CommandWithLocator> getClickableOrVisible() {
        if (clickableOrVisible == null) {
            clickableOrVisible = new ArrayList<CommandWithLocator>();
        }
        return this.clickableOrVisible;
    }

}
