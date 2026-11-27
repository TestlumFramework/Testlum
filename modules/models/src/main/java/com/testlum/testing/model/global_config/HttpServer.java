package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>Java class for httpServer complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="httpServer"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="host" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString" default="127.0.0.1" minOccurs="0"/&gt;
 *         &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" default="58743" minOccurs="0"/&gt;
 *         &lt;element name="maxThreads" type="{http://www.testlum.com/testing/model/global-config}positiveIntegerMin1" default="50" minOccurs="0"/&gt;
 *         &lt;element name="ngrok" type="{http://www.testlum.com/testing/model/global-config}ngrok" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "httpServer", propOrder = {
        "host",
        "port",
        "maxThreads",
        "ngrok"
})
public class HttpServer {

    @XmlAttribute(name = "enabled", required = true)
    protected boolean enabled;
    protected String host;
    protected Integer port;
    protected Integer maxThreads;
    protected Ngrok ngrok;

    /**
     * Gets the value of the enabled property.
     *
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     *
     */
    public void setEnabled(boolean value) {
        this.enabled = value;
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
        if (host == null) {
            return "127.0.0.1";
        } else {
            return host;
        }
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
     *     {@link Integer }
     *
     */
    public Integer getPort() {
        if (port == null) {
            return 58743;
        } else {
            return port;
        }
    }

    /**
     * Sets the value of the port property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setPort(Integer value) {
        this.port = value;
    }

    /**
     * Gets the value of the maxThreads property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getMaxThreads() {
        if (maxThreads == null) {
            return 50;
        } else {
            return maxThreads;
        }
    }

    /**
     * Sets the value of the maxThreads property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setMaxThreads(Integer value) {
        this.maxThreads = value;
    }

    /**
     * Gets the value of the ngrok property.
     *
     * @return
     *     possible object is
     *     {@link Ngrok }
     *
     */
    public Ngrok getNgrok() {
        return ngrok;
    }

    /**
     * Sets the value of the ngrok property.
     *
     * @param value
     *     allowed object is
     *     {@link Ngrok }
     *
     */
    public void setNgrok(Ngrok value) {
        this.ngrok = value;
    }
}
