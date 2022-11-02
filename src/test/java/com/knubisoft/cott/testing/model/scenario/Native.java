
package com.knubisoft.cott.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for native complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="native"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractUiTag"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="click" type="{http://www.knubisoft.com/cott/testing/model/scenario}click"/&gt;
 *         &lt;element name="input" type="{http://www.knubisoft.com/cott/testing/model/scenario}input"/&gt;
 *         &lt;element name="assert" type="{http://www.knubisoft.com/cott/testing/model/scenario}assert"/&gt;
 *         &lt;element name="wait" type="{http://www.knubisoft.com/cott/testing/model/scenario}wait"/&gt;
 *         &lt;element name="clear" type="{http://www.knubisoft.com/cott/testing/model/scenario}clear"/&gt;
 *         &lt;element name="image" type="{http://www.knubisoft.com/cott/testing/model/scenario}image"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "native", propOrder = {
    "clickOrInputOrAssert"
})
public class Native
    extends AbstractUiTag
{

    @XmlElements({
        @XmlElement(name = "click", type = Click.class),
        @XmlElement(name = "input", type = Input.class),
        @XmlElement(name = "assert", type = Assert.class),
        @XmlElement(name = "wait", type = Wait.class),
        @XmlElement(name = "clear", type = Clear.class),
        @XmlElement(name = "image", type = Image.class)
    })
    protected List<AbstractUiCommand> clickOrInputOrAssert;

    /**
     * Gets the value of the clickOrInputOrAssert property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clickOrInputOrAssert property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClickOrInputOrAssert().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Click }
     * {@link Input }
     * {@link Assert }
     * {@link Wait }
     * {@link Clear }
     * {@link Image }
     * 
     * 
     */
    public List<AbstractUiCommand> getClickOrInputOrAssert() {
        if (clickOrInputOrAssert == null) {
            clickOrInputOrAssert = new ArrayList<AbstractUiCommand>();
        }
        return this.clickOrInputOrAssert;
    }

}
