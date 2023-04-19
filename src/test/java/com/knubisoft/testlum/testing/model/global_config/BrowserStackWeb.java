
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for browserStackWeb complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="browserStackWeb"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="browserVersion" use="required" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "browserStackWeb")
public class BrowserStackWeb {

    @XmlAttribute(name = "browserVersion", required = true)
    protected String browserVersion;

    /**
     * Gets the value of the browserVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBrowserVersion() {
        return browserVersion;
    }

    /**
     * Sets the value of the browserVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBrowserVersion(String value) {
        this.browserVersion = value;
    }

}
