
package com.knubisoft.cott.testing.model.scenario;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for repeatUiCommand complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="repeatUiCommand"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="click" type="{http://www.knubisoft.com/cott/testing/model/scenario}click"/&gt;
 *         &lt;element name="input" type="{http://www.knubisoft.com/cott/testing/model/scenario}input"/&gt;
 *         &lt;element name="navigate" type="{http://www.knubisoft.com/cott/testing/model/scenario}navigate"/&gt;
 *         &lt;element name="assert" type="{http://www.knubisoft.com/cott/testing/model/scenario}assert"/&gt;
 *         &lt;element name="dropDown" type="{http://www.knubisoft.com/cott/testing/model/scenario}dropDown"/&gt;
 *         &lt;element name="javascript" type="{http://www.knubisoft.com/cott/testing/model/scenario}javascript"/&gt;
 *         &lt;element name="wait" type="{http://www.knubisoft.com/cott/testing/model/scenario}wait"/&gt;
 *         &lt;element name="clear" type="{http://www.knubisoft.com/cott/testing/model/scenario}clear"/&gt;
 *         &lt;element name="closeSecondTab" type="{http://www.knubisoft.com/cott/testing/model/scenario}closeSecondTab"/&gt;
 *         &lt;element name="scroll" type="{http://www.knubisoft.com/cott/testing/model/scenario}scroll"/&gt;
 *         &lt;element name="scrollTo" type="{http://www.knubisoft.com/cott/testing/model/scenario}scrollTo"/&gt;
 *         &lt;element name="image" type="{http://www.knubisoft.com/cott/testing/model/scenario}image"/&gt;
 *         &lt;element name="repeat" type="{http://www.knubisoft.com/cott/testing/model/scenario}repeatUiCommand"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="times" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "repeatUiCommand", propOrder = {
    "clickOrInputOrNavigate"
})
public class RepeatUiCommand
    extends AbstractCommand
{

    @XmlElements({
        @XmlElement(name = "click", type = Click.class),
        @XmlElement(name = "input", type = Input.class),
        @XmlElement(name = "navigate", type = Navigate.class),
        @XmlElement(name = "assert", type = Assert.class),
        @XmlElement(name = "dropDown", type = DropDown.class),
        @XmlElement(name = "javascript", type = Javascript.class),
        @XmlElement(name = "wait", type = Wait.class),
        @XmlElement(name = "clear", type = Clear.class),
        @XmlElement(name = "closeSecondTab", type = CloseSecondTab.class),
        @XmlElement(name = "scroll", type = Scroll.class),
        @XmlElement(name = "scrollTo", type = ScrollTo.class),
        @XmlElement(name = "image", type = Image.class),
        @XmlElement(name = "repeat", type = RepeatUiCommand.class)
    })
    protected List<AbstractCommand> clickOrInputOrNavigate;
    @XmlAttribute(name = "times", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger times;

    /**
     * Gets the value of the clickOrInputOrNavigate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clickOrInputOrNavigate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClickOrInputOrNavigate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Click }
     * {@link Input }
     * {@link Navigate }
     * {@link Assert }
     * {@link DropDown }
     * {@link Javascript }
     * {@link Wait }
     * {@link Clear }
     * {@link CloseSecondTab }
     * {@link Scroll }
     * {@link ScrollTo }
     * {@link Image }
     * {@link RepeatUiCommand }
     * 
     * 
     */
    public List<AbstractCommand> getClickOrInputOrNavigate() {
        if (clickOrInputOrNavigate == null) {
            clickOrInputOrNavigate = new ArrayList<AbstractCommand>();
        }
        return this.clickOrInputOrNavigate;
    }

    /**
     * Gets the value of the times property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTimes() {
        return times;
    }

    /**
     * Sets the value of the times property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTimes(BigInteger value) {
        this.times = value;
    }

}
