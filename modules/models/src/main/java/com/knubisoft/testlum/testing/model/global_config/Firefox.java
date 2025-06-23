
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for firefox complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="firefox"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}abstractBrowser"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="firefoxOptionsArguments" type="{http://www.knubisoft.com/testlum/testing/model/global-config}browserOptionsArguments" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="headlessMode" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "firefox", propOrder = {
    "firefoxOptionsArguments"
})
public class Firefox
    extends AbstractBrowser
{

    protected BrowserOptionsArguments firefoxOptionsArguments;
    @XmlAttribute(name = "headlessMode", required = true)
    protected boolean headlessMode;

    /**
     * Gets the value of the firefoxOptionsArguments property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserOptionsArguments }
     *     
     */
    public BrowserOptionsArguments getFirefoxOptionsArguments() {
        return firefoxOptionsArguments;
    }

    /**
     * Sets the value of the firefoxOptionsArguments property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserOptionsArguments }
     *     
     */
    public void setFirefoxOptionsArguments(BrowserOptionsArguments value) {
        this.firefoxOptionsArguments = value;
    }

    /**
     * Gets the value of the headlessMode property.
     * 
     */
    public boolean isHeadlessMode() {
        return headlessMode;
    }

    /**
     * Sets the value of the headlessMode property.
     * 
     */
    public void setHeadlessMode(boolean value) {
        this.headlessMode = value;
    }

}
