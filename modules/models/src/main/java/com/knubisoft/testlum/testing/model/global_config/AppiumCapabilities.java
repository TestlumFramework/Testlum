
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for appiumCapabilities complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="appiumCapabilities"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}abstractCapabilities"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="udid" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "appiumCapabilities", propOrder = {
    "udid"
})
@XmlSeeAlso({
    AppiumNativeCapabilities.class
})
public class AppiumCapabilities
    extends AbstractCapabilities
{

    protected String udid;

    /**
     * Gets the value of the udid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUdid() {
        return udid;
    }

    /**
     * Sets the value of the udid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUdid(String value) {
        this.udid = value;
    }

}
