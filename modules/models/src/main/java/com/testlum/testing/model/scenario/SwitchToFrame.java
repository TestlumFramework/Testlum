
package com.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for switchToFrame complex type&lt;/p&gt;.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="switchToFrame"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/scenario}commandWithOptionalLocator"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="click" type="{http://www.testlum.com/testing/model/scenario}click"/&gt;
 *         &lt;element name="input" type="{http://www.testlum.com/testing/model/scenario}input"/&gt;
 *         &lt;element name="assert" type="{http://www.testlum.com/testing/model/scenario}webAssert"/&gt;
 *         &lt;element name="dropDown" type="{http://www.testlum.com/testing/model/scenario}dropDown"/&gt;
 *         &lt;element name="wait" type="{http://www.testlum.com/testing/model/scenario}uiWait"/&gt;
 *         &lt;element name="clear" type="{http://www.testlum.com/testing/model/scenario}clear"/&gt;
 *         &lt;element name="scroll" type="{http://www.testlum.com/testing/model/scenario}scroll"/&gt;
 *         &lt;element name="scrollTo" type="{http://www.testlum.com/testing/model/scenario}scrollTo"/&gt;
 *         &lt;element name="image" type="{http://www.testlum.com/testing/model/scenario}image"/&gt;
 *         &lt;element name="javascript" type="{http://www.testlum.com/testing/model/scenario}javascript"/&gt;
 *         &lt;element name="navigate" type="{http://www.testlum.com/testing/model/scenario}navigate"/&gt;
 *         &lt;element name="hover" type="{http://www.testlum.com/testing/model/scenario}hover"/&gt;
 *         &lt;element name="tab" type="{http://www.testlum.com/testing/model/scenario}browserTab"/&gt;
 *         &lt;element name="switchToFrame" type="{http://www.testlum.com/testing/model/scenario}switchToFrame"/&gt;
 *         &lt;element name="dragAndDrop" type="{http://www.testlum.com/testing/model/scenario}dragAndDrop"/&gt;
 *         &lt;element name="hotKey" type="{http://www.testlum.com/testing/model/scenario}hotKey"/&gt;
 *         &lt;element name="var" type="{http://www.testlum.com/testing/model/scenario}webVar"/&gt;
 *         &lt;element name="condition" type="{http://www.testlum.com/testing/model/scenario}uiCondition"/&gt;
 *         &lt;element name="doubleClick" type="{http://www.testlum.com/testing/model/scenario}doubleClick"/&gt;
 *         &lt;element name="alert" type="{http://www.testlum.com/testing/model/scenario}alert"/&gt;
 *         &lt;element name="ott" type="{http://www.testlum.com/testing/model/scenario}uiOtt"/&gt;
 *         &lt;element name="ottInput" type="{http://www.testlum.com/testing/model/scenario}ottInput"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="index" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
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
        @XmlElement(name = "condition", type = UiCondition.class),
        @XmlElement(name = "doubleClick", type = DoubleClick.class),
        @XmlElement(name = "alert", type = Alert.class),
        @XmlElement(name = "ott", type = UiOtt.class),
        @XmlElement(name = "ottInput", type = OttInput.class)
    })
    protected List<AbstractUiCommand> clickOrInputOrAssert;
    @XmlAttribute(name = "index")
    protected String index;

    /**
     * Gets the value of the clickOrInputOrAssert property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the clickOrInputOrAssert property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getClickOrInputOrAssert().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Alert }
     * {@link BrowserTab }
     * {@link Clear }
     * {@link Click }
     * {@link DoubleClick }
     * {@link DragAndDrop }
     * {@link DropDown }
     * {@link HotKey }
     * {@link Hover }
     * {@link Image }
     * {@link Input }
     * {@link Javascript }
     * {@link Navigate }
     * {@link OttInput }
     * {@link Scroll }
     * {@link ScrollTo }
     * {@link SwitchToFrame }
     * {@link UiCondition }
     * {@link UiOtt }
     * {@link UiWait }
     * {@link WebAssert }
     * {@link WebVar }
     * </p>
     * 
     * 
     * @return
     *     The value of the clickOrInputOrAssert property.
     */
    public List<AbstractUiCommand> getClickOrInputOrAssert() {
        if (clickOrInputOrAssert == null) {
            clickOrInputOrAssert = new ArrayList<>();
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
