
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for localBrowser complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="localBrowser"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="driverVersion" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "localBrowser")
public class LocalBrowser {

    @XmlAttribute(name = "driverVersion")
    protected String driverVersion;

    /**
     * Gets the value of the driverVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDriverVersion() {
        return driverVersion;
    }

    /**
     * Sets the value of the driverVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDriverVersion(String value) {
        this.driverVersion = value;
    }

}
