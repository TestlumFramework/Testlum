
package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for browserStackNativeCapabilities complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="browserStackNativeCapabilities"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/global-config}browserStackCapabilities"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="app" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "browserStackNativeCapabilities", propOrder = {
    "app"
})
public class BrowserStackNativeCapabilities
    extends BrowserStackCapabilities
{

    @XmlElement(required = true)
    protected String app;

    /**
     * Gets the value of the app property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApp() {
        return app;
    }

    /**
     * Sets the value of the app property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApp(String value) {
        this.app = value;
    }

}
