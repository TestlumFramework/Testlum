
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for clickhouse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="clickhouse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}storageIntegration"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="jdbcDriver" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="username" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="password" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="connectionUrl" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "clickhouse", propOrder = {
    "jdbcDriver",
    "username",
    "password",
    "connectionUrl"
})
public class Clickhouse
    extends StorageIntegration
{

    @XmlElement(required = true)
    protected String jdbcDriver;
    @XmlElement(required = true)
    protected String username;
    @XmlElement(required = true)
    protected String password;
    @XmlElement(required = true)
    protected String connectionUrl;

    /**
     * Gets the value of the jdbcDriver property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJdbcDriver() {
        return jdbcDriver;
    }

    /**
     * Sets the value of the jdbcDriver property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJdbcDriver(String value) {
        this.jdbcDriver = value;
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
     * Gets the value of the connectionUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionUrl() {
        return connectionUrl;
    }

    /**
     * Sets the value of the connectionUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionUrl(String value) {
        this.connectionUrl = value;
    }

}
