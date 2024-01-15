
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dragAndDropNative complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dragAndDropNative"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;attribute name="fromLocator" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="fromLocatorStrategy" type="{http://www.knubisoft.com/testlum/testing/model/scenario}locatorStrategy" default="locator" /&gt;
 *       &lt;attribute name="toLocator" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="toLocatorStrategy" type="{http://www.knubisoft.com/testlum/testing/model/scenario}locatorStrategy" default="locator" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dragAndDropNative")
public class DragAndDropNative
    extends AbstractUiCommand
{

    @XmlAttribute(name = "fromLocator", required = true)
    protected String fromLocator;
    @XmlAttribute(name = "fromLocatorStrategy")
    protected LocatorStrategy fromLocatorStrategy;
    @XmlAttribute(name = "toLocator", required = true)
    protected String toLocator;
    @XmlAttribute(name = "toLocatorStrategy")
    protected LocatorStrategy toLocatorStrategy;

    /**
     * Gets the value of the fromLocator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFromLocator() {
        return fromLocator;
    }

    /**
     * Sets the value of the fromLocator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromLocator(String value) {
        this.fromLocator = value;
    }

    /**
     * Gets the value of the fromLocatorStrategy property.
     * 
     * @return
     *     possible object is
     *     {@link LocatorStrategy }
     *     
     */
    public LocatorStrategy getFromLocatorStrategy() {
        if (fromLocatorStrategy == null) {
            return LocatorStrategy.LOCATOR;
        } else {
            return fromLocatorStrategy;
        }
    }

    /**
     * Sets the value of the fromLocatorStrategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocatorStrategy }
     *     
     */
    public void setFromLocatorStrategy(LocatorStrategy value) {
        this.fromLocatorStrategy = value;
    }

    /**
     * Gets the value of the toLocator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToLocator() {
        return toLocator;
    }

    /**
     * Sets the value of the toLocator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToLocator(String value) {
        this.toLocator = value;
    }

    /**
     * Gets the value of the toLocatorStrategy property.
     * 
     * @return
     *     possible object is
     *     {@link LocatorStrategy }
     *     
     */
    public LocatorStrategy getToLocatorStrategy() {
        if (toLocatorStrategy == null) {
            return LocatorStrategy.LOCATOR;
        } else {
            return toLocatorStrategy;
        }
    }

    /**
     * Sets the value of the toLocatorStrategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocatorStrategy }
     *     
     */
    public void setToLocatorStrategy(LocatorStrategy value) {
        this.toLocatorStrategy = value;
    }

}
