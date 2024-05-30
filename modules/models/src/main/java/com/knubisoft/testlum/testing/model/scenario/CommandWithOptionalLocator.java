
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for commandWithOptionalLocator complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="commandWithOptionalLocator"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;attribute name="locatorStrategy" type="{http://www.knubisoft.com/testlum/testing/model/scenario}locatorStrategy" default="locatorId" /&gt;
 *       &lt;attribute name="locator" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "commandWithOptionalLocator")
@XmlSeeAlso({
    Scroll.class,
    SwitchToFrame.class,
    Paste.class,
    Highlight.class,
    SwipeNative.class
})
public abstract class CommandWithOptionalLocator
    extends AbstractUiCommand
{

    @XmlAttribute(name = "locatorStrategy")
    protected LocatorStrategy locatorStrategy;
    @XmlAttribute(name = "locator")
    protected String locator;

    /**
     * Gets the value of the locatorStrategy property.
     * 
     * @return
     *     possible object is
     *     {@link LocatorStrategy }
     *     
     */
    public LocatorStrategy getLocatorStrategy() {
        if (locatorStrategy == null) {
            return LocatorStrategy.LOCATOR_ID;
        } else {
            return locatorStrategy;
        }
    }

    /**
     * Sets the value of the locatorStrategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocatorStrategy }
     *     
     */
    public void setLocatorStrategy(LocatorStrategy value) {
        this.locatorStrategy = value;
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
