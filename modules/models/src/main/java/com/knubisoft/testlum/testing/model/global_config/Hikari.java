
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for hikari complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="hikari"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="connectionTimeout" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="idleTimeout" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="maxLifetime" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="maximumPoolSize" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="minimumIdle" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="connectionInitSql" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="connectionTestQuery" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="poolName" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="autoCommit" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hikari", propOrder = {
    "connectionTimeout",
    "idleTimeout",
    "maxLifetime",
    "maximumPoolSize",
    "minimumIdle",
    "connectionInitSql",
    "connectionTestQuery",
    "poolName",
    "autoCommit"
})
public class Hikari {

    protected int connectionTimeout;
    protected int idleTimeout;
    protected int maxLifetime;
    protected int maximumPoolSize;
    protected int minimumIdle;
    @XmlElement(required = true)
    protected String connectionInitSql;
    @XmlElement(required = true)
    protected String connectionTestQuery;
    @XmlElement(required = true)
    protected String poolName;
    protected boolean autoCommit;

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
     * Gets the value of the idleTimeout property.
     * 
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Sets the value of the idleTimeout property.
     * 
     */
    public void setIdleTimeout(int value) {
        this.idleTimeout = value;
    }

    /**
     * Gets the value of the maxLifetime property.
     * 
     */
    public int getMaxLifetime() {
        return maxLifetime;
    }

    /**
     * Sets the value of the maxLifetime property.
     * 
     */
    public void setMaxLifetime(int value) {
        this.maxLifetime = value;
    }

    /**
     * Gets the value of the maximumPoolSize property.
     * 
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * Sets the value of the maximumPoolSize property.
     * 
     */
    public void setMaximumPoolSize(int value) {
        this.maximumPoolSize = value;
    }

    /**
     * Gets the value of the minimumIdle property.
     * 
     */
    public int getMinimumIdle() {
        return minimumIdle;
    }

    /**
     * Sets the value of the minimumIdle property.
     * 
     */
    public void setMinimumIdle(int value) {
        this.minimumIdle = value;
    }

    /**
     * Gets the value of the connectionInitSql property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionInitSql() {
        return connectionInitSql;
    }

    /**
     * Sets the value of the connectionInitSql property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionInitSql(String value) {
        this.connectionInitSql = value;
    }

    /**
     * Gets the value of the connectionTestQuery property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionTestQuery() {
        return connectionTestQuery;
    }

    /**
     * Sets the value of the connectionTestQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionTestQuery(String value) {
        this.connectionTestQuery = value;
    }

    /**
     * Gets the value of the poolName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * Sets the value of the poolName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPoolName(String value) {
        this.poolName = value;
    }

    /**
     * Gets the value of the autoCommit property.
     * 
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Sets the value of the autoCommit property.
     * 
     */
    public void setAutoCommit(boolean value) {
        this.autoCommit = value;
    }

}
