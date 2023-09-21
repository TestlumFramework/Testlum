
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for settings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="settings"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="variations" type="{http://www.knubisoft.com/testlum/testing/model/scenario}csv" minOccurs="0"/&gt;
 *         &lt;element name="tags" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="active" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="onlyThis" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="truncateStorages" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "settings", propOrder = {
    "variations",
    "tags"
})
public class Settings {

    protected String variations;
    protected String tags;
    @XmlAttribute(name = "active")
    protected Boolean active;
    @XmlAttribute(name = "onlyThis")
    protected Boolean onlyThis;
    @XmlAttribute(name = "truncateStorages")
    protected Boolean truncateStorages;

    /**
     * Gets the value of the variations property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVariations() {
        return variations;
    }

    /**
     * Sets the value of the variations property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVariations(String value) {
        this.variations = value;
    }

    /**
     * Gets the value of the tags property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTags() {
        return tags;
    }

    /**
     * Sets the value of the tags property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTags(String value) {
        this.tags = value;
    }

    /**
     * Gets the value of the active property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isActive() {
        if (active == null) {
            return true;
        } else {
            return active;
        }
    }

    /**
     * Sets the value of the active property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setActive(Boolean value) {
        this.active = value;
    }

    /**
     * Gets the value of the onlyThis property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOnlyThis() {
        if (onlyThis == null) {
            return false;
        } else {
            return onlyThis;
        }
    }

    /**
     * Sets the value of the onlyThis property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOnlyThis(Boolean value) {
        this.onlyThis = value;
    }

    /**
     * Gets the value of the truncateStorages property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTruncateStorages() {
        if (truncateStorages == null) {
            return false;
        } else {
            return truncateStorages;
        }
    }

    /**
     * Sets the value of the truncateStorages property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTruncateStorages(Boolean value) {
        this.truncateStorages = value;
    }

}
