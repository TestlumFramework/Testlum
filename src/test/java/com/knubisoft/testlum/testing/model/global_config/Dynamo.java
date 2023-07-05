
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dynamo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dynamo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}storageIntegration"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="region" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="endpoint" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="accessKeyId" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="secretAccessKey" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="sessionToken" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dynamo", propOrder = {
    "region",
    "endpoint",
    "accessKeyId",
    "secretAccessKey",
    "sessionToken"
})
public class Dynamo
    extends StorageIntegration
{

    @XmlElement(required = true)
    protected String region;
    @XmlElement(required = true)
    protected String endpoint;
    @XmlElement(required = true)
    protected String accessKeyId;
    @XmlElement(required = true)
    protected String secretAccessKey;
    @XmlElement(required = true)
    protected String sessionToken;

    /**
     * Gets the value of the region property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the value of the region property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegion(String value) {
        this.region = value;
    }

    /**
     * Gets the value of the endpoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the value of the endpoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndpoint(String value) {
        this.endpoint = value;
    }

    /**
     * Gets the value of the accessKeyId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccessKeyId() {
        return accessKeyId;
    }

    /**
     * Sets the value of the accessKeyId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccessKeyId(String value) {
        this.accessKeyId = value;
    }

    /**
     * Gets the value of the secretAccessKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    /**
     * Sets the value of the secretAccessKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecretAccessKey(String value) {
        this.secretAccessKey = value;
    }

    /**
     * Gets the value of the sessionToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Sets the value of the sessionToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionToken(String value) {
        this.sessionToken = value;
    }

}
