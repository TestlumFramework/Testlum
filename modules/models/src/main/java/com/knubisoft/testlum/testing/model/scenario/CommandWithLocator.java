
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for commandWithLocator complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="commandWithLocator"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;attribute name="locatorStrategy" type="{http://www.knubisoft.com/testlum/testing/model/scenario}locatorStrategy" default="locatorId" /&gt;
 *       &lt;attribute name="locator" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "commandWithLocator")
@XmlSeeAlso({
    Clickable.class,
    Visible.class,
    AssertChecked.class,
    AssertPresent.class,
    Click.class,
    DoubleClick.class,
    Hover.class,
    Input.class,
    DropDown.class,
    Clear.class,
    ScrollTo.class,
    SwipeElement.class
})
public abstract class CommandWithLocator
    extends AbstractUiCommand
{

    @XmlAttribute(name = "locatorStrategy")
    protected LocatorStrategy locatorStrategy;
    @XmlAttribute(name = "locator", required = true)
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
