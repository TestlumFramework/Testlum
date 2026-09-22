
package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.*;


/**
 * &lt;p&gt;Java class for testlumReportServer complex type&lt;/p&gt;.
 * <p>
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 * <p>
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="testlumReportServer"&gt;
 * &lt;complexContent&gt;
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 * &lt;sequence&gt;
 * &lt;element name="serverUrl" type="{http://www.testlum.com/testing/model/global-config}url"/&gt;
 * &lt;element name="apiKey" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString"/&gt;
 * &lt;element name="rabbitConfig" type="{http://www.testlum.com/testing/model/global-config}rabbitConfig"/&gt;
 * &lt;/sequence&gt;
 * &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 * &lt;/restriction&gt;
 * &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testlumReportServer", propOrder = {
        "serverUrl",
        "apiKey",
        "rabbitConfig"
})
public class TestlumReportServer {

    @XmlElement(required = true)
    protected String serverUrl;
    @XmlElement(required = true)
    protected String apiKey;
    @XmlElement(required = true)
    protected RabbitConfig rabbitConfig;
    @XmlAttribute(name = "enabled")
    protected Boolean enabled;

    /**
     * Gets the value of the serverUrl property.
     *
     * @return possible object is
     * {@link String }
     *
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Sets the value of the serverUrl property.
     *
     * @param value allowed object is
     *              {@link String }
     *
     */
    public void setServerUrl(String value) {
        this.serverUrl = value;
    }

    /**
     * Gets the value of the apiKey property.
     *
     * @return possible object is
     * {@link String }
     *
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the value of the apiKey property.
     *
     * @param value allowed object is
     *              {@link String }
     *
     */
    public void setApiKey(String value) {
        this.apiKey = value;
    }

    /**
     * Gets the value of the rabbitConfig property.
     *
     * @return possible object is
     * {@link RabbitConfig }
     *
     */
    public RabbitConfig getRabbitConfig() {
        return rabbitConfig;
    }

    /**
     * Sets the value of the rabbitConfig property.
     *
     * @param value allowed object is
     *              {@link RabbitConfig }
     *
     */
    public void setRabbitConfig(RabbitConfig value) {
        this.rabbitConfig = value;
    }

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is
     * {@link Boolean }
     *
     */
    public boolean isEnabled() {
        if (enabled == null) {
            return true;
        } else {
            return enabled;
        }
    }

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     *
     */
    public void setEnabled(Boolean value) {
        this.enabled = value;
    }

}
