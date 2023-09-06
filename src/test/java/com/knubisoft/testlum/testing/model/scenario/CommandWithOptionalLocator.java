
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
 *       &lt;attribute name="locatorId" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
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
    Paste.class,
    Highlight.class,
    SwipeNative.class
})
public abstract class CommandWithOptionalLocator
    extends AbstractUiCommand
{

    @XmlAttribute(name = "locatorStrategy")
    protected LocatorStrategy locatorStrategy;
    @XmlAttribute(name = "locatorId")
    protected String locatorId;

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
     * Gets the value of the locatorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocatorId() {
        return locatorId;
    }

    /**
     * Sets the value of the locatorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocatorId(String value) {
        this.locatorId = value;
    }

}
