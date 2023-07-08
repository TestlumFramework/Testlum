
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
 *         &lt;element name="compareWithFullScreen" type="{http://www.knubisoft.com/testlum/testing/model/scenario}compareWithFullScreen"/&gt;
 *         &lt;element name="compareWithImage" type="{http://www.knubisoft.com/testlum/testing/model/scenario}compareWithImage"/&gt;
 *         &lt;element name="compareWithElement" type="{http://www.knubisoft.com/testlum/testing/model/scenario}compareWithElement"/&gt;
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
    "compareWithFullScreen",
    "compareWithImage",
    "compareWithElement"
})
public class Image
    extends AbstractUiCommand
{

    protected CompareWithFullScreen compareWithFullScreen;
    protected CompareWithImage compareWithImage;
    protected CompareWithElement compareWithElement;
    @XmlAttribute(name = "file", required = true)
    protected String file;
    @XmlAttribute(name = "highlightDifference")
    protected Boolean highlightDifference;

    /**
     * Gets the value of the compareWithFullScreen property.
     * 
     * @return
     *     possible object is
     *     {@link CompareWithFullScreen }
     *     
     */
    public CompareWithFullScreen getCompareWithFullScreen() {
        return compareWithFullScreen;
    }

    /**
     * Sets the value of the compareWithFullScreen property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompareWithFullScreen }
     *     
     */
    public void setCompareWithFullScreen(CompareWithFullScreen value) {
        this.compareWithFullScreen = value;
    }

    /**
     * Gets the value of the compareWithImage property.
     * 
     * @return
     *     possible object is
     *     {@link CompareWithImage }
     *     
     */
    public CompareWithImage getCompareWithImage() {
        return compareWithImage;
    }

    /**
     * Sets the value of the compareWithImage property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompareWithImage }
     *     
     */
    public void setCompareWithImage(CompareWithImage value) {
        this.compareWithImage = value;
    }

    /**
     * Gets the value of the compareWithElement property.
     * 
     * @return
     *     possible object is
     *     {@link CompareWithElement }
     *     
     */
    public CompareWithElement getCompareWithElement() {
        return compareWithElement;
    }

    /**
     * Sets the value of the compareWithElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompareWithElement }
     *     
     */
    public void setCompareWithElement(CompareWithElement value) {
        this.compareWithElement = value;
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
