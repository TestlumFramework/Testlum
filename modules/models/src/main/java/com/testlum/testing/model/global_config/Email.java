
package com.testlum.testing.model.global_config;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for email complex type&lt;/p&gt;.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="email"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/global-config}integration"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="protocol" type="{http://www.testlum.com/testing/model/global-config}emailProtocol"/&gt;
 *         &lt;element name="host" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/&gt;
 *         &lt;element name="emailAddress" type="{http://www.testlum.com/testing/model/global-config}emailAddressPattern"/&gt;
 *         &lt;element name="password" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="folder" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="ssl" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="timeout" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/&gt;
 *         &lt;element name="properties" type="{http://www.testlum.com/testing/model/global-config}emailProperties" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "email", propOrder = {
    "protocol",
    "host",
    "port",
    "emailAddress",
    "password",
    "folder",
    "ssl",
    "timeout",
    "properties"
})
public class Email
    extends Integration
{

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected EmailProtocol protocol;
    @XmlElement(required = true)
    protected String host;
    @XmlElement(required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger port;
    @XmlElement(required = true)
    protected String emailAddress;
    @XmlElement(required = true)
    protected String password;
    @XmlElement(required = true)
    protected String folder;
    @XmlElement(required = true)
    protected Boolean ssl;
    @XmlElement(required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger timeout;
    protected EmailProperties properties;

    /**
     * Gets the value of the protocol property.
     * 
     * @return
     *     possible object is
     *     {@link EmailProtocol }
     *     
     */
    public EmailProtocol getProtocol() {
        return protocol;
    }

    /**
     * Sets the value of the protocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailProtocol }
     *     
     */
    public void setProtocol(EmailProtocol value) {
        this.protocol = value;
    }

    /**
     * Gets the value of the host property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the value of the host property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHost(String value) {
        this.host = value;
    }

    /**
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPort(BigInteger value) {
        this.port = value;
    }

    /**
     * Gets the value of the emailAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the value of the emailAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailAddress(final String value) {
        this.emailAddress = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the folder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Sets the value of the folder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFolder(String value) {
        this.folder = value;
    }

    /**
     * Gets the value of the ssl property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSsl() {
        return ssl;
    }

    /**
     * Sets the value of the ssl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSsl(Boolean value) {
        this.ssl = value;
    }

    /**
     * Gets the value of the timeout property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTimeout() {
        return timeout;
    }

    /**
     * Sets the value of the timeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTimeout(BigInteger value) {
        this.timeout = value;
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link EmailProperties }
     *     
     */
    public EmailProperties getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailProperties }
     *     
     */
    public void setProperties(EmailProperties value) {
        this.properties = value;
    }

}
