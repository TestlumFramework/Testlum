
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for webTab complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="webTab"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="close" type="{http://www.knubisoft.com/testlum/testing/model/scenario}closeTab"/&gt;
 *         &lt;element name="open" type="{http://www.knubisoft.com/testlum/testing/model/scenario}openTab"/&gt;
 *         &lt;element name="switch" type="{http://www.knubisoft.com/testlum/testing/model/scenario}switchTab"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webTab", propOrder = {
    "close",
    "open",
    "_switch"
})
public class WebTab
    extends AbstractUiCommand
{

    protected CloseTab close;
    protected OpenTab open;
    @XmlElement(name = "switch")
    protected SwitchTab _switch;

    /**
     * Gets the value of the close property.
     * 
     * @return
     *     possible object is
     *     {@link CloseTab }
     *     
     */
    public CloseTab getClose() {
        return close;
    }

    /**
     * Sets the value of the close property.
     * 
     * @param value
     *     allowed object is
     *     {@link CloseTab }
     *     
     */
    public void setClose(CloseTab value) {
        this.close = value;
    }

    /**
     * Gets the value of the open property.
     * 
     * @return
     *     possible object is
     *     {@link OpenTab }
     *     
     */
    public OpenTab getOpen() {
        return open;
    }

    /**
     * Sets the value of the open property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenTab }
     *     
     */
    public void setOpen(OpenTab value) {
        this.open = value;
    }

    /**
     * Gets the value of the switch property.
     * 
     * @return
     *     possible object is
     *     {@link SwitchTab }
     *     
     */
    public SwitchTab getSwitch() {
        return _switch;
    }

    /**
     * Sets the value of the switch property.
     * 
     * @param value
     *     allowed object is
     *     {@link SwitchTab }
     *     
     */
    public void setSwitch(SwitchTab value) {
        this._switch = value;
    }

}
