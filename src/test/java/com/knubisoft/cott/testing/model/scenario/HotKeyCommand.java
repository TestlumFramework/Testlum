
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for hotKeyCommand complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="hotKeyCommand"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractCommand"&gt;
 *       &lt;attribute name="hotKeyAction" type="{http://www.knubisoft.com/cott/testing/model/scenario}hotKeyAction" /&gt;
 *       &lt;attribute name="singleKeyAction" type="{http://www.knubisoft.com/cott/testing/model/scenario}singleKeyAction" /&gt;
 *       &lt;attribute name="locator" type="{http://www.knubisoft.com/cott/testing/model/scenario}scenarioLocator" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hotKeyCommand")
public class HotKeyCommand
    extends AbstractCommand
{

    @XmlAttribute(name = "hotKeyAction")
    protected HotKeyAction hotKeyAction;
    @XmlAttribute(name = "singleKeyAction")
    protected SingleKeyAction singleKeyAction;
    @XmlAttribute(name = "locator")
    protected String locator;

    /**
     * Gets the value of the hotKeyAction property.
     * 
     * @return
     *     possible object is
     *     {@link HotKeyAction }
     *     
     */
    public HotKeyAction getHotKeyAction() {
        return hotKeyAction;
    }

    /**
     * Sets the value of the hotKeyAction property.
     * 
     * @param value
     *     allowed object is
     *     {@link HotKeyAction }
     *     
     */
    public void setHotKeyAction(HotKeyAction value) {
        this.hotKeyAction = value;
    }

    /**
     * Gets the value of the singleKeyAction property.
     * 
     * @return
     *     possible object is
     *     {@link SingleKeyAction }
     *     
     */
    public SingleKeyAction getSingleKeyAction() {
        return singleKeyAction;
    }

    /**
     * Sets the value of the singleKeyAction property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleKeyAction }
     *     
     */
    public void setSingleKeyAction(SingleKeyAction value) {
        this.singleKeyAction = value;
    }

    /**
     * Gets the value of the locator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocator() {
        return locator;
    }

    /**
     * Sets the value of the locator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocator(String value) {
        this.locator = value;
    }

}
