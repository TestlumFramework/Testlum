
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for rabbitmq complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="rabbitmq"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}storageIntegration"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="host" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="username" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="password" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="apiPort" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="virtualHost" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="enabledMetrics" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rabbitmq", propOrder = {
    "host",
    "port",
    "username",
    "password",
    "apiPort",
    "virtualHost",
    "enabledMetrics"
})
public class Rabbitmq
    extends StorageIntegration
{

    @XmlElement(required = true)
    protected String host;
    protected int port;
    @XmlElement(required = true)
    protected String username;
    @XmlElement(required = true)
    protected String password;
    protected int apiPort;
    @XmlElement(required = true)
    protected String virtualHost;
    protected boolean enabledMetrics;

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

    /**
     * Gets the value of the apiPort property.
     * 
     */
    public int getApiPort() {
        return apiPort;
    }

    /**
     * Sets the value of the apiPort property.
     * 
     */
    public void setApiPort(int value) {
        this.apiPort = value;
    }

    /**
     * Gets the value of the virtualHost property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVirtualHost() {
        return virtualHost;
    }

    /**
     * Sets the value of the virtualHost property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVirtualHost(String value) {
        this.virtualHost = value;
    }

    /**
     * Gets the value of the enabledMetrics property.
     * 
     */
    public boolean isEnabledMetrics() {
        return enabledMetrics;
    }

    /**
     * Sets the value of the enabledMetrics property.
     * 
     */
    public void setEnabledMetrics(boolean value) {
        this.enabledMetrics = value;
    }

}
