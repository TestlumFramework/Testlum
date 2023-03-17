
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for part complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="part"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="text" type="{http://www.knubisoft.com/cott/testing/model/scenario}partText"/&gt;
 *         &lt;element name="file" type="{http://www.knubisoft.com/cott/testing/model/scenario}partFile"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "part", propOrder = {
    "text",
    "file"
})
public class Part {

    protected PartText text;
    protected PartFile file;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link PartText }
     *     
     */
    public PartText getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link PartText }
     *     
     */
    public void setText(PartText value) {
        this.text = value;
    }

    /**
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link PartFile }
     *     
     */
    public PartFile getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link PartFile }
     *     
     */
    public void setFile(PartFile value) {
        this.file = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
