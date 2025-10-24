
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mobileImage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mobileImage"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="fullScreen" type="{http://www.knubisoft.com/testlum/testing/model/scenario}fullScreen"/&gt;
 *         &lt;element name="picture" type="{http://www.knubisoft.com/testlum/testing/model/scenario}picture"/&gt;
 *         &lt;element name="part" type="{http://www.knubisoft.com/testlum/testing/model/scenario}part"/&gt;
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
@XmlType(name = "mobileImage", propOrder = {
    "fullScreen",
    "picture",
    "part"
})
public class MobileImage
    extends AbstractUiCommand
{

    protected FullScreen fullScreen;
    protected Picture picture;
    protected Part part;
    @XmlAttribute(name = "file", required = true)
    protected String file;
    @XmlAttribute(name = "highlightDifference")
    protected Boolean highlightDifference;

    /**
     * Gets the value of the fullScreen property.
     * 
     * @return
     *     possible object is
     *     {@link FullScreen }
     *     
     */
    public FullScreen getFullScreen() {
        return fullScreen;
    }

    /**
     * Sets the value of the fullScreen property.
     * 
     * @param value
     *     allowed object is
     *     {@link FullScreen }
     *     
     */
    public void setFullScreen(FullScreen value) {
        this.fullScreen = value;
    }

    /**
     * Gets the value of the picture property.
     * 
     * @return
     *     possible object is
     *     {@link Picture }
     *     
     */
    public Picture getPicture() {
        return picture;
    }

    /**
     * Sets the value of the picture property.
     * 
     * @param value
     *     allowed object is
     *     {@link Picture }
     *     
     */
    public void setPicture(Picture value) {
        this.picture = value;
    }

    /**
     * Gets the value of the part property.
     * 
     * @return
     *     possible object is
     *     {@link Part }
     *     
     */
    public Part getPart() {
        return part;
    }

    /**
     * Sets the value of the part property.
     * 
     * @param value
     *     allowed object is
     *     {@link Part }
     *     
     */
    public void setPart(Part value) {
        this.part = value;
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
