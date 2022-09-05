
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for abstractDataSource complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="abstractDataSource"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/global-config}integration"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="jdbcDriver" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="username" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="password" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="connectionUrl" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="schema" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "abstractDataSource", propOrder = {
    "jdbcDriver",
    "username",
    "password",
    "connectionUrl",
    "schema"
})
@XmlSeeAlso({
    Clickhouse.class,
    CommonDataSource.class
})
public abstract class AbstractDataSource
    extends Integration
{

    @XmlElement(required = true)
    protected String jdbcDriver;
    @XmlElement(required = true)
    protected String username;
    @XmlElement(required = true)
    protected String password;
    @XmlElement(required = true)
    protected String connectionUrl;
    protected String schema;

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

    /**
     * Gets the value of the schema property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the value of the schema property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchema(String value) {
        this.schema = value;
    }

}
