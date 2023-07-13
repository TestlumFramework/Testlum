
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for image complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="image"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="fullScreen" type="{http://www.knubisoft.com/testlum/testing/model/scenario}compareWithFullScreen"/&gt;
 *         &lt;element name="element" type="{http://www.knubisoft.com/testlum/testing/model/scenario}compareWithElement"/&gt;
 *         &lt;element name="part" type="{http://www.knubisoft.com/testlum/testing/model/scenario}compareWithPart"/&gt;
 *         &lt;element name="findPart" type="{http://www.knubisoft.com/testlum/testing/model/scenario}findPart"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="file" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}imageExtension" /&gt;
 *       &lt;attribute name="highlightDifference" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "image", propOrder = {
    "fullScreen",
    "element",
    "part",
    "findPart"
})
public class Image
    extends AbstractUiCommand
{

    protected CompareWithFullScreen fullScreen;
    protected CompareWithElement element;
    protected CompareWithPart part;
    protected FindPart findPart;
    @XmlAttribute(name = "file", required = true)
    protected String file;
    @XmlAttribute(name = "highlightDifference")
    protected Boolean highlightDifference;

    /**
     * Gets the value of the fullScreen property.
     * 
     * @return
     *     possible object is
     *     {@link CompareWithFullScreen }
     *     
     */
    public CompareWithFullScreen getFullScreen() {
        return fullScreen;
    }

    /**
     * Sets the value of the fullScreen property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompareWithFullScreen }
     *     
     */
    public void setFullScreen(CompareWithFullScreen value) {
        this.fullScreen = value;
    }

    /**
     * Gets the value of the element property.
     * 
     * @return
     *     possible object is
     *     {@link CompareWithElement }
     *     
     */
    public CompareWithElement getElement() {
        return element;
    }

    /**
     * Sets the value of the element property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompareWithElement }
     *     
     */
    public void setElement(CompareWithElement value) {
        this.element = value;
    }

    /**
     * Gets the value of the part property.
     * 
     * @return
     *     possible object is
     *     {@link CompareWithPart }
     *     
     */
    public CompareWithPart getPart() {
        return part;
    }

    /**
     * Sets the value of the part property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompareWithPart }
     *     
     */
    public void setPart(CompareWithPart value) {
        this.part = value;
    }

    /**
     * Gets the value of the findPart property.
     * 
     * @return
     *     possible object is
     *     {@link FindPart }
     *     
     */
    public FindPart getFindPart() {
        return findPart;
    }

    /**
     * Sets the value of the findPart property.
     * 
     * @param value
     *     allowed object is
     *     {@link FindPart }
     *     
     */
    public void setFindPart(FindPart value) {
        this.findPart = value;
    }

    /**
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFile(String value) {
        this.file = value;
    }

    /**
     * Gets the value of the highlightDifference property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHighlightDifference() {
        if (highlightDifference == null) {
            return false;
        } else {
            return highlightDifference;
        }
    }

    /**
     * Sets the value of the highlightDifference property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHighlightDifference(Boolean value) {
        this.highlightDifference = value;
    }

}
