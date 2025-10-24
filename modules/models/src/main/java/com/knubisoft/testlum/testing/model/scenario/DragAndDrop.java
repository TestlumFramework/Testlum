
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dragAndDrop complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dragAndDrop"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="fromLocator" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="fileName" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="toLocator" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="toLocatorStrategy" type="{http://www.knubisoft.com/testlum/testing/model/scenario}locatorStrategy" default="locatorId" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dragAndDrop", propOrder = {
    "fromLocator",
    "fileName"
})
public class DragAndDrop
    extends AbstractUiCommand
{

    protected String fromLocator;
    protected String fileName;
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
     * Gets the value of the fileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the value of the fileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileName(String value) {
        this.fileName = value;
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
            return LocatorStrategy.LOCATOR_ID;
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
