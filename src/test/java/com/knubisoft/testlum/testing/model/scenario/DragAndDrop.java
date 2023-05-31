
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="fromLocatorId" type="{http://www.knubisoft.com/testlum/testing/model/scenario}scenarioLocator"/&gt;
 *         &lt;element name="fileName" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="toLocatorId" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}scenarioLocator" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dragAndDrop", propOrder = {
    "fromLocatorId",
    "fileName"
})
public class DragAndDrop
    extends AbstractUiCommand
{

    protected String fromLocatorId;
    protected String fileName;
    @XmlAttribute(name = "toLocatorId", required = true)
    protected String toLocatorId;

    /**
     * Gets the value of the fromLocatorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFromLocatorId() {
        return fromLocatorId;
    }

    /**
     * Sets the value of the fromLocatorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromLocatorId(String value) {
        this.fromLocatorId = value;
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
     * Gets the value of the toLocatorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToLocatorId() {
        return toLocatorId;
    }

    /**
     * Sets the value of the toLocatorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToLocatorId(String value) {
        this.toLocatorId = value;
    }

}
