
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for partFile complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="partFile"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.knubisoft.com/cott/testing/model/scenario&gt;nonEmptyString"&gt;
 *       &lt;attribute name="filename" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="contentType" type="{http://www.knubisoft.com/cott/testing/model/scenario}contentType" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partFile", propOrder = {
    "value"
})
public class PartFile {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "filename")
    @XmlSchemaType(name = "anySimpleType")
    protected String filename;
    @XmlAttribute(name = "contentType")
    protected ContentType contentType;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the filename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the value of the filename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilename(String value) {
        this.filename = value;
    }

    /**
     * Gets the value of the contentType property.
     * 
     * @return
     *     possible object is
     *     {@link ContentType }
     *     
     */
    public ContentType getContentType() {
        return contentType;
    }

    /**
     * Sets the value of the contentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContentType }
     *     
     */
    public void setContentType(ContentType value) {
        this.contentType = value;
    }

}
