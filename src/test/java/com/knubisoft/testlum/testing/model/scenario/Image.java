
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
 *         &lt;element name="findIn" type="{http://www.knubisoft.com/testlum/testing/model/scenario}findIn"/&gt;
 *         &lt;element name="compareWith" type="{http://www.knubisoft.com/testlum/testing/model/scenario}compareWith"/&gt;
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
    "findIn",
    "compareWith"
})
public class Image
    extends AbstractUiCommand
{

    protected FindIn findIn;
    protected CompareWith compareWith;
    @XmlAttribute(name = "file", required = true)
    protected String file;
    @XmlAttribute(name = "highlightDifference")
    protected Boolean highlightDifference;

    /**
     * Gets the value of the findIn property.
     * 
     * @return
     *     possible object is
     *     {@link FindIn }
     *     
     */
    public FindIn getFindIn() {
        return findIn;
    }

    /**
     * Sets the value of the findIn property.
     * 
     * @param value
     *     allowed object is
     *     {@link FindIn }
     *     
     */
    public void setFindIn(FindIn value) {
        this.findIn = value;
    }

    /**
     * Gets the value of the compareWith property.
     * 
     * @return
     *     possible object is
     *     {@link CompareWith }
     *     
     */
    public CompareWith getCompareWith() {
        return compareWith;
    }

    /**
     * Sets the value of the compareWith property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompareWith }
     *     
     */
    public void setCompareWith(CompareWith value) {
        this.compareWith = value;
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
