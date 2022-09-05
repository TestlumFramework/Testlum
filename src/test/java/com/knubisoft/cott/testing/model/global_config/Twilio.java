
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for twilio complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="twilio"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/global-config}integration"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="accountSid" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="authToken" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="twilioNumber" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "twilio", propOrder = {
    "accountSid",
    "authToken",
    "twilioNumber"
})
public class Twilio
    extends Integration
{

    @XmlElement(required = true)
    protected String accountSid;
    @XmlElement(required = true)
    protected String authToken;
    @XmlElement(required = true)
    protected String twilioNumber;

    /**
     * Gets the value of the accountSid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccountSid() {
        return accountSid;
    }

    /**
     * Sets the value of the accountSid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccountSid(String value) {
        this.accountSid = value;
    }

    /**
     * Gets the value of the authToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Sets the value of the authToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthToken(String value) {
        this.authToken = value;
    }

    /**
     * Gets the value of the twilioNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTwilioNumber() {
        return twilioNumber;
    }

    /**
     * Sets the value of the twilioNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTwilioNumber(String value) {
        this.twilioNumber = value;
    }

}
