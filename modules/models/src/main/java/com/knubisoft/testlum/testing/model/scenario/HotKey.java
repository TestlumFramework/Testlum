
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for hotKey complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="hotKey"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="copy" type="{http://www.knubisoft.com/testlum/testing/model/scenario}copy"/&gt;
 *         &lt;element name="paste" type="{http://www.knubisoft.com/testlum/testing/model/scenario}paste"/&gt;
 *         &lt;element name="cut" type="{http://www.knubisoft.com/testlum/testing/model/scenario}cut"/&gt;
 *         &lt;element name="highlight" type="{http://www.knubisoft.com/testlum/testing/model/scenario}highlight"/&gt;
 *         &lt;element name="tab" type="{http://www.knubisoft.com/testlum/testing/model/scenario}tab"/&gt;
 *         &lt;element name="enter" type="{http://www.knubisoft.com/testlum/testing/model/scenario}enter"/&gt;
 *         &lt;element name="escape" type="{http://www.knubisoft.com/testlum/testing/model/scenario}escape"/&gt;
 *         &lt;element name="space" type="{http://www.knubisoft.com/testlum/testing/model/scenario}space"/&gt;
 *         &lt;element name="backSpace" type="{http://www.knubisoft.com/testlum/testing/model/scenario}backSpace"/&gt;
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
    "copyOrPasteOrCut"
})
public class HotKey
    extends AbstractUiCommand
{

    @XmlElements({
        @XmlElement(name = "copy", type = Copy.class),
        @XmlElement(name = "paste", type = Paste.class),
        @XmlElement(name = "cut", type = Cut.class),
        @XmlElement(name = "highlight", type = Highlight.class),
        @XmlElement(name = "tab", type = Tab.class),
        @XmlElement(name = "enter", type = Enter.class),
        @XmlElement(name = "escape", type = Escape.class),
        @XmlElement(name = "space", type = Space.class),
        @XmlElement(name = "backSpace", type = BackSpace.class)
    })
    protected List<AbstractUiCommand> copyOrPasteOrCut;

    /**
     * Gets the value of the copyOrPasteOrCut property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the copyOrPasteOrCut property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCopyOrPasteOrCut().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Copy }
     * {@link Paste }
     * {@link Cut }
     * {@link Highlight }
     * {@link Tab }
     * {@link Enter }
     * {@link Escape }
     * {@link Space }
     * {@link BackSpace }
     * 
     * 
     */
    public List<AbstractUiCommand> getCopyOrPasteOrCut() {
        if (copyOrPasteOrCut == null) {
            copyOrPasteOrCut = new ArrayList<AbstractUiCommand>();
        }
        return this.copyOrPasteOrCut;
    }

}
