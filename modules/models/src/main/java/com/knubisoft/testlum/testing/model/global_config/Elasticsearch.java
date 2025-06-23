
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for elasticsearch complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="elasticsearch"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}storageIntegration"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="host" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="scheme" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="connectionTimeout" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="socketTimeout" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="signer" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="serviceName" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="region" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="username" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="password" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "elasticsearch", propOrder = {
    "host",
    "port",
    "scheme",
    "connectionTimeout",
    "socketTimeout",
    "signer",
    "serviceName",
    "region",
    "username",
    "password"
})
public class Elasticsearch
    extends StorageIntegration
{

    @XmlElement(required = true)
    protected String host;
    protected int port;
    @XmlElement(required = true)
    protected String scheme;
    protected int connectionTimeout;
    protected int socketTimeout;
    protected boolean signer;
    @XmlElement(required = true)
    protected String serviceName;
    @XmlElement(required = true)
    protected String region;
    @XmlElement(required = true)
    protected String username;
    @XmlElement(required = true)
    protected String password;

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
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     */
    public void setPort(int value) {
        this.port = value;
    }

    /**
     * Gets the value of the scheme property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the value of the scheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScheme(String value) {
        this.scheme = value;
    }

    /**
     * Gets the value of the connectionTimeout property.
     * 
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the value of the connectionTimeout property.
     * 
     */
    public void setConnectionTimeout(int value) {
        this.connectionTimeout = value;
    }

    /**
     * Gets the value of the socketTimeout property.
     * 
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Sets the value of the socketTimeout property.
     * 
     */
    public void setSocketTimeout(int value) {
        this.socketTimeout = value;
    }

    /**
     * Gets the value of the signer property.
     * 
     */
    public boolean isSigner() {
        return signer;
    }

    /**
     * Sets the value of the signer property.
     * 
     */
    public void setSigner(boolean value) {
        this.signer = value;
    }

    /**
     * Gets the value of the serviceName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the value of the serviceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceName(String value) {
        this.serviceName = value;
    }

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
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsername(String value) {
        this.username = value;
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

}
