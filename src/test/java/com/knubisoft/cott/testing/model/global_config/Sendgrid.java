
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sendgrid complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendgrid"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/global-config}integration"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="apiKey" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendgrid", propOrder = {
    "apiKey"
})
public class Sendgrid
    extends Integration
{

    @XmlElement(required = true)
    protected String apiKey;

    /**
     * Gets the value of the apiKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the value of the apiKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApiKey(String value) {
        this.apiKey = value;
    }

}
