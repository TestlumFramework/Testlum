
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for browserSettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="browserSettings"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}settings"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="browsers" type="{http://www.knubisoft.com/testlum/testing/model/global-config}browsers"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "browserSettings", propOrder = {
    "browsers"
})
public class BrowserSettings
    extends Settings
{

    @XmlElement(required = true)
    protected Browsers browsers;

    /**
     * Gets the value of the browsers property.
     * 
     * @return
     *     possible object is
     *     {@link Browsers }
     *     
     */
    public Browsers getBrowsers() {
        return browsers;
    }

    /**
     * Sets the value of the browsers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Browsers }
     *     
     */
    public void setBrowsers(Browsers value) {
        this.browsers = value;
    }

}
