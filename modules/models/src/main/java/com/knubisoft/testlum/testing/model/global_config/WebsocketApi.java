
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for websocketApi complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="websocketApi"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}integration"&gt;
 *       &lt;attribute name="url" use="required" type="{http://www.knubisoft.com/testlum/testing/model/global-config}urlWebsocket" /&gt;
 *       &lt;attribute name="protocol" use="required" type="{http://www.knubisoft.com/testlum/testing/model/global-config}websocketProtocol" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "websocketApi")
public class WebsocketApi
    extends Integration
{

    @XmlAttribute(name = "url", required = true)
    protected String url;
    @XmlAttribute(name = "protocol", required = true)
    protected WebsocketProtocol protocol;

    /**
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the protocol property.
     * 
     * @return
     *     possible object is
     *     {@link WebsocketProtocol }
     *     
     */
    public WebsocketProtocol getProtocol() {
        return protocol;
    }

    /**
     * Sets the value of the protocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebsocketProtocol }
     *     
     */
    public void setProtocol(WebsocketProtocol value) {
        this.protocol = value;
    }

}
