
package com.knubisoft.cott.testing.model.pages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for text complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="text"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.knubisoft.com/cott/testing/model/pages&gt;nonEmptyString"&gt;
 *       &lt;attribute name="placeholder" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "text", propOrder = {
    "value"
})
public class Text {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "placeholder")
    protected Boolean placeholder;

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
     * Gets the value of the placeholder property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPlaceholder() {
        if (placeholder == null) {
            return false;
        } else {
            return placeholder;
        }
    }

    /**
     * Sets the value of the placeholder property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPlaceholder(Boolean value) {
        this.placeholder = value;
    }

}
