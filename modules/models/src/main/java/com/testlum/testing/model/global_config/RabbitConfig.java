
package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for rabbitConfig complex type&lt;/p&gt;.
 * <p>
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 * <p>
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="rabbitConfig"&gt;
 * &lt;complexContent&gt;
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 * &lt;sequence&gt;
 * &lt;element name="host" type="{http://www.testlum.com/testing/model/global-config}hostNameOrIPv4"/&gt;
 * &lt;element name="port" type="{http://www.testlum.com/testing/model/global-config}port"/&gt;
 * &lt;element name="username" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString"/&gt;
 * &lt;element name="password" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString"/&gt;
 * &lt;element name="virtualHost" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString"/&gt;
 * &lt;/sequence&gt;
 * &lt;/restriction&gt;
 * &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rabbitConfig", propOrder = {
        "host",
        "port",
        "username",
        "password",
        "virtualHost"
})
public class RabbitConfig {

    @XmlElement(required = true)
    protected String host;
    @XmlElement(required = true, defaultValue = "5672")
    protected String port;
    @XmlElement(required = true)
    protected String username;
    @XmlElement(required = true)
    protected String password;
    @XmlElement(required = true, defaultValue = "/")
    protected String virtualHost;

    /**
     * Gets the value of the host property.
     *
     * @return possible object is
     * {@link String }
     *
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the value of the host property.
     *
     * @param value allowed object is
     *              {@link String }
     *
     */
    public void setHost(String value) {
        this.host = value;
    }

    /**
     * Gets the value of the port property.
     *
     * @return possible object is
     * {@link String }
     *
     */
    public String getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     *
     * @param value allowed object is
     *              {@link String }
     *
     */
    public void setPort(String value) {
        this.port = value;
    }

    /**
     * Gets the value of the username property.
     *
     * @return possible object is
     * {@link String }
     *
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     *
     * @param value allowed object is
     *              {@link String }
     *
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the password property.
     *
     * @return possible object is
     * {@link String }
     *
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     *
     * @param value allowed object is
     *              {@link String }
     *
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the virtualHost property.
     *
     * @return possible object is
     * {@link String }
     *
     */
    public String getVirtualHost() {
        return virtualHost;
    }

    /**
     * Sets the value of the virtualHost property.
     *
     * @param value allowed object is
     *              {@link String }
     *
     */
    public void setVirtualHost(String value) {
        this.virtualHost = value;
    }

}
