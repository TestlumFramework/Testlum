
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlElement;


/**
 * <p>Java class for customTruncateConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="customTruncateConfig"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="truncateFile" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "customTruncateConfig", propOrder = {
    "truncateFile"
})
public class CustomTruncateConfig {

    @XmlElement(required = true)
    protected String truncateFile;

    /**
     * Gets the value of the truncateFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTruncateFile() {
        return truncateFile;
    }

    /**
     * Sets the value of the truncateFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTruncateFile(String value) {
        this.truncateFile = value;
    }

}
