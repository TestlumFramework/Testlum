
package com.knubisoft.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for switchToFrame complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="switchToFrame"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}commandWithOptionalLocator"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="click" type="{http://www.knubisoft.com/testlum/testing/model/scenario}click"/&gt;
 *         &lt;element name="input" type="{http://www.knubisoft.com/testlum/testing/model/scenario}input"/&gt;
 *         &lt;element name="assert" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webAssert"/&gt;
 *         &lt;element name="dropDown" type="{http://www.knubisoft.com/testlum/testing/model/scenario}dropDown"/&gt;
 *         &lt;element name="wait" type="{http://www.knubisoft.com/testlum/testing/model/scenario}uiWait"/&gt;
 *         &lt;element name="clear" type="{http://www.knubisoft.com/testlum/testing/model/scenario}clear"/&gt;
 *         &lt;element name="scroll" type="{http://www.knubisoft.com/testlum/testing/model/scenario}scroll"/&gt;
 *         &lt;element name="scrollTo" type="{http://www.knubisoft.com/testlum/testing/model/scenario}scrollTo"/&gt;
 *         &lt;element name="image" type="{http://www.knubisoft.com/testlum/testing/model/scenario}image"/&gt;
 *         &lt;element name="javascript" type="{http://www.knubisoft.com/testlum/testing/model/scenario}javascript"/&gt;
 *         &lt;element name="navigate" type="{http://www.knubisoft.com/testlum/testing/model/scenario}navigate"/&gt;
 *         &lt;element name="hover" type="{http://www.knubisoft.com/testlum/testing/model/scenario}hover"/&gt;
 *         &lt;element name="tab" type="{http://www.knubisoft.com/testlum/testing/model/scenario}browserTab"/&gt;
 *         &lt;element name="switchToFrame" type="{http://www.knubisoft.com/testlum/testing/model/scenario}switchToFrame"/&gt;
 *         &lt;element name="dragAndDrop" type="{http://www.knubisoft.com/testlum/testing/model/scenario}dragAndDrop"/&gt;
 *         &lt;element name="hotKey" type="{http://www.knubisoft.com/testlum/testing/model/scenario}hotKey"/&gt;
 *         &lt;element name="var" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webVar"/&gt;
 *         &lt;element name="condition" type="{http://www.knubisoft.com/testlum/testing/model/scenario}uiCondition"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="index" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "switchToFrame", propOrder = {
    "clickOrInputOrAssert"
})
public class SwitchToFrame
    extends CommandWithOptionalLocator
{

    @XmlElements({
        @XmlElement(name = "click", type = Click.class),
        @XmlElement(name = "input", type = Input.class),
        @XmlElement(name = "assert", type = WebAssert.class),
        @XmlElement(name = "dropDown", type = DropDown.class),
        @XmlElement(name = "wait", type = UiWait.class),
        @XmlElement(name = "clear", type = Clear.class),
        @XmlElement(name = "scroll", type = Scroll.class),
        @XmlElement(name = "scrollTo", type = ScrollTo.class),
        @XmlElement(name = "image", type = Image.class),
        @XmlElement(name = "javascript", type = Javascript.class),
        @XmlElement(name = "navigate", type = Navigate.class),
        @XmlElement(name = "hover", type = Hover.class),
        @XmlElement(name = "tab", type = BrowserTab.class),
        @XmlElement(name = "switchToFrame", type = SwitchToFrame.class),
        @XmlElement(name = "dragAndDrop", type = DragAndDrop.class),
        @XmlElement(name = "hotKey", type = HotKey.class),
        @XmlElement(name = "var", type = WebVar.class),
        @XmlElement(name = "condition", type = UiCondition.class)
    })
    protected List<AbstractUiCommand> clickOrInputOrAssert;
    @XmlAttribute(name = "index")
    protected String index;

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
     * {@link WebAssert }
     * {@link DropDown }
     * {@link UiWait }
     * {@link Clear }
     * {@link Scroll }
     * {@link ScrollTo }
     * {@link Image }
     * {@link Javascript }
     * {@link Navigate }
     * {@link Hover }
     * {@link BrowserTab }
     * {@link SwitchToFrame }
     * {@link DragAndDrop }
     * {@link HotKey }
     * {@link WebVar }
     * {@link UiCondition }
     * 
     * 
     */
    public List<AbstractUiCommand> getClickOrInputOrAssert() {
        if (clickOrInputOrAssert == null) {
            clickOrInputOrAssert = new ArrayList<AbstractUiCommand>();
        }
        return this.clickOrInputOrAssert;
    }

    /**
     * Gets the value of the index property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIndex(String value) {
        this.index = value;
    }

}
