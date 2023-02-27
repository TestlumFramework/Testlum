
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fromCookie complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fromCookie"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="browserType" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}variableBrowserType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fromCookie")
public class FromCookie {

    @XmlAttribute(name = "browserType", required = true)
    protected VariableBrowserType browserType;

    /**
     * Gets the value of the browserType property.
     * 
     * @return
     *     possible object is
     *     {@link VariableBrowserType }
     *     
     */
    public VariableBrowserType getBrowserType() {
        return browserType;
    }

    /**
     * Sets the value of the browserType property.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableBrowserType }
     *     
     */
    public void setBrowserType(VariableBrowserType value) {
        this.browserType = value;
    }

}
