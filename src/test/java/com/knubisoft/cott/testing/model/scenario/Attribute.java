
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for attribute complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="attribute"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *       &lt;attribute name="outerHTML" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="class" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="id" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="xpath" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="cssSelector" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="linkText" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="partialLinkText" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "attribute", propOrder = {
    "value"
})
public class Attribute {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "outerHTML")
    protected String outerHTML;
    @XmlAttribute(name = "class")
    protected String clazz;
    @XmlAttribute(name = "id")
    protected String id;
    @XmlAttribute(name = "xpath")
    protected String xpath;
    @XmlAttribute(name = "cssSelector")
    protected String cssSelector;
    @XmlAttribute(name = "linkText")
    protected String linkText;
    @XmlAttribute(name = "partialLinkText")
    protected String partialLinkText;

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
     * Gets the value of the outerHTML property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOuterHTML() {
        return outerHTML;
    }

    /**
     * Sets the value of the outerHTML property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOuterHTML(String value) {
        this.outerHTML = value;
    }

    /**
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the xpath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXpath() {
        return xpath;
    }

    /**
     * Sets the value of the xpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXpath(String value) {
        this.xpath = value;
    }

    /**
     * Gets the value of the cssSelector property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCssSelector() {
        return cssSelector;
    }

    /**
     * Sets the value of the cssSelector property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCssSelector(String value) {
        this.cssSelector = value;
    }

    /**
     * Gets the value of the linkText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkText() {
        return linkText;
    }

    /**
     * Sets the value of the linkText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkText(String value) {
        this.linkText = value;
    }

    /**
     * Gets the value of the partialLinkText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPartialLinkText() {
        return partialLinkText;
    }

    /**
     * Sets the value of the partialLinkText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPartialLinkText(String value) {
        this.partialLinkText = value;
    }

}
