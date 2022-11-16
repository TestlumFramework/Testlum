
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for drag-and-drop complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="drag-and-drop"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="fromLocatorId" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="filePath" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="toLocatorId" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="dropFile" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "drag-and-drop", propOrder = {
    "fromLocatorId",
    "filePath"
})
public class DragAndDrop
    extends AbstractUiCommand
{

    protected String fromLocatorId;
    protected String filePath;
    @XmlAttribute(name = "toLocatorId", required = true)
    protected String toLocatorId;
    @XmlAttribute(name = "dropFile", required = true)
    protected boolean dropFile;

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
     * Gets the value of the filePath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets the value of the filePath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilePath(String value) {
        this.filePath = value;
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

    /**
     * Gets the value of the dropFile property.
     * 
     */
    public boolean isDropFile() {
        return dropFile;
    }

    /**
     * Sets the value of the dropFile property.
     * 
     */
    public void setDropFile(boolean value) {
        this.dropFile = value;
    }

}
