
package com.knubisoft.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for assert complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="assert"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="attribute" type="{http://www.knubisoft.com/testlum/testing/model/scenario}attribute"/&gt;
 *         &lt;element name="title" type="{http://www.knubisoft.com/testlum/testing/model/scenario}title"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "assert", propOrder = {
    "attributeOrTitle"
})
public class Assert
    extends AbstractUiCommand
{

    @XmlElements({
        @XmlElement(name = "attribute", type = Attribute.class),
        @XmlElement(name = "title", type = Title.class)
    })
    protected List<AbstractUiCommand> attributeOrTitle;

    /**
     * Gets the value of the attributeOrTitle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeOrTitle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeOrTitle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Attribute }
     * {@link Title }
     * 
     * 
     */
    public List<AbstractUiCommand> getAttributeOrTitle() {
        if (attributeOrTitle == null) {
            attributeOrTitle = new ArrayList<AbstractUiCommand>();
        }
        return this.attributeOrTitle;
    }

}
